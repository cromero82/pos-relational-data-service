package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.entities.HistorialProducto;
import com.infinitesoft.pos_relational_data_service.entities.Product;
import com.infinitesoft.pos_relational_data_service.repositories.ProductRepository;
import com.infinitesoft.pos_relational_data_service.services.HistorialProductoService;
import com.infinitesoft.pos_relational_data_service.services.MigrationResult;
import com.infinitesoft.pos_relational_data_service.services.MigrationService;
import com.infinitesoft.pos_relational_data_service.services.ProductService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class MigrationServiceImpl implements MigrationService {

    private static final Logger log = LoggerFactory.getLogger(MigrationServiceImpl.class);

    private static final String BARCODE_REGEX = "^\\d{5,30}$"; // Numeric barcode: 5–30 digits (broad support for various local/legacy formats)

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private HistorialProductoService historialProductoService;

    @Override
    public MigrationResult importVentasYa(MultipartFile file, String eventName) {
        if (file == null || file.isEmpty()) {
            return MigrationResult.error("Empty file");
        }
        if (eventName == null || eventName.isBlank()) {
            return MigrationResult.error("Missing eventName");
        }

        int created = 0;
        int skipped = 0;
        int duplicates = 0;
        int errors = 0;
        List<String> messages = new ArrayList<>();

        String filename = file.getOriginalFilename();
        log.info("[IMPORT] Starting importVentasYa | event='{}' | file='{}'", eventName, filename);

        try {
            // Read all bytes to allow charset detection
            byte[] bytes = file.getBytes();
            Charset detected = detectCharset(bytes);
            String content = new String(bytes, detected);
            if (log.isInfoEnabled()) {
                log.info("[IMPORT] Using charset '{}' for file='{}'", detected.name(), filename);
            }
            // Remove potential UTF-8 BOM from content start
            if (!content.isEmpty() && content.charAt(0) == '\uFEFF') {
                content = content.substring(1);
            }
            try (BufferedReader br = new BufferedReader(new StringReader(content))) {
                String line;
                int lineNo = 0;
                while ((line = br.readLine()) != null) {
                    lineNo++;
                    String raw = line.trim();
                    if (raw.isEmpty()) {
                        log.debug("[IMPORT] Line {}: empty/blank, skipped", lineNo);
                        continue;
                    }

                if (looksLikeHeader(raw)) {
                    // Skip header row
                    log.debug("[IMPORT] Line {}: header detected, skipped: {}", lineNo, raw);
                    continue;
                }

                log.trace("[IMPORT] Line {} RAW: {}", lineNo, raw);

                String[] cols = splitFlexible(raw);
                if (cols.length < 2) {
                    skipped++;
                    // Only log, do not add to messages (messages should contain only failed imports)
                    log.warn("[IMPORT] Line {}: not enough columns ({}), skipped | cols={} | raw='{}'", lineNo, cols.length, Arrays.toString(cols), raw);
                    continue;
                }

                String first = safeGet(cols, 0);
                boolean firstIsBarcode = first != null && first.matches(BARCODE_REGEX);
                String second = safeGet(cols, 1);
                boolean secondIsBarcode = second != null && second.matches(BARCODE_REGEX);

                String codigo;
                String descripcion;
                String precioCostoStr;
                String precioVentaStr;
                String pendingMsg = null; // message to append later with full context

                // Explicit handling for 8+ column lines where code/description might be inverted or both are descriptions
                if (cols.length >= 8 && first != null && second != null && (firstIsBarcode ^ secondIsBarcode)) {
                    if (firstIsBarcode) {
                        // Regular order: [0]=barcode, [1]=description
                        codigo = first;
                        descripcion = second;
                        precioCostoStr = safeGet(cols, 2);
                        precioVentaStr = safeGet(cols, 3);
                        log.debug("[IMPORT] Line {}: scenario=BARCODE+DESC (8+) | codigo='{}' | descripcion='{}'", lineNo, codigo, descripcion);
                    } else {
                        // Inverted order: [0]=description, [1]=barcode
                        codigo = second;
                        descripcion = first;
                        precioCostoStr = safeGet(cols, 2);
                        precioVentaStr = safeGet(cols, 3);
                        log.debug("[IMPORT] Line {}: scenario=INVERTED (8+) | codigo='{}' | descripcion='{}'", lineNo, codigo, descripcion);
                    }
                } else if (first != null && second != null && !firstIsBarcode && !secondIsBarcode && !looksLikeMoney(second) && cols.length >= 4) {
                    // New scenario: both first and second look like descriptions (no valid barcode). Use first as 'codigo' and second as 'descripcion'. Applies for 4+ columns.
                    codigo = first;
                    descripcion = second;
                    precioCostoStr = safeGet(cols, 2);
                    precioVentaStr = safeGet(cols, 3);
                    pendingMsg = "both first tokens are not barcode – using first as codigo and second as descripcion";
                    log.debug("[IMPORT] Line {}: {} | codigo='{}' | descripcion='{}'", lineNo, pendingMsg, codigo, descripcion);
                } else if (!firstIsBarcode) {
                    // Scenario: first column is description, no barcode provided (or ambiguous)
                    codigo = "";
                    descripcion = first;
                    precioCostoStr = safeGet(cols, 1);
                    precioVentaStr = safeGet(cols, 2);
                    log.debug("[IMPORT] Line {}: scenario=NO_BARCODE descriptionOnly | descripcion='{}'", lineNo, descripcion);
                } else if (cols.length == 7) {
                    // Scenario: barcode present but description missing (7 columns total)
                    codigo = first;
                    descripcion = "";
                    precioCostoStr = safeGet(cols, 1);
                    precioVentaStr = safeGet(cols, 2);
                    pendingMsg = "barcode '" + codigo + "' without description – registered with warning";
                    log.warn("[IMPORT] Line {}: {}", lineNo, pendingMsg);
                } else {
                    // Default: barcode and description present in first two columns
                    codigo = first;
                    descripcion = safeGet(cols, 1);
                    precioCostoStr = safeGet(cols, 2);
                    precioVentaStr = safeGet(cols, 3);
                    log.debug("[IMPORT] Line {}: scenario=BARCODE+DESC | codigo='{}' | descripcion='{}'", lineNo, codigo, descripcion);
                }

                // Fallback to heuristics if parsed tokens don't look like money
                if (!looksLikeMoney(precioCostoStr)) {
                    String before = precioCostoStr;
                    precioCostoStr = findPrecioCosto(cols);
                    log.debug("[IMPORT] Line {}: resolved precioCosto from '{}' -> '{}' via heuristic", lineNo, before, precioCostoStr);
                }
                if (!looksLikeMoney(precioVentaStr)) {
                    String before = precioVentaStr;
                    precioVentaStr = findPrecioVenta(cols);
                    log.debug("[IMPORT] Line {}: resolved precioVenta from '{}' -> '{}' via heuristic", lineNo, before, precioVentaStr);
                }

                // After resolving prices: do not include non-failure info/warnings in messages; just log context for diagnostics
                if (pendingMsg != null) {
                    String ctxSummary = "codigo='" + codigo + "', descripcion='" + descripcion + "', precioCosto='" + precioCostoStr + "', precioVenta='" + precioVentaStr + "', cols=" + Arrays.toString(cols);
                    log.debug("[IMPORT] Line {}: {} | {}", lineNo, pendingMsg, ctxSummary);
                }

                Double precioVenta = parseMoneyToDouble(precioVentaStr).orElse(0.0);
                Double precioCosto = parseMoneyToDouble(precioCostoStr).orElse(0.0);
                log.debug("[IMPORT] Line {}: parsed precioCosto={} precioVenta={}", lineNo, precioCosto, precioVenta);

                // Check duplicates only if a non-empty barcode is provided
                if (codigo != null && !codigo.isBlank()) {
                    String normalizedBarcode = codigo.toUpperCase(Locale.ROOT);
                    if (productRepository.findByBarcode(normalizedBarcode).isPresent()) {
                        duplicates++;
                        String ctxSummary = "codigo='" + codigo + "', descripcion='" + descripcion + "', precioCosto='" + precioCostoStr + "', precioVenta='" + precioVentaStr + "', cols=" + Arrays.toString(cols);
                        String dupMsg = "Line " + lineNo + ": duplicate barcode '" + codigo + "' – skipped | " + ctxSummary;
                        // Do not add to messages; duplicates are not failures
                        log.warn("[IMPORT] {}", dupMsg);
                        continue;
                    }
                }

                try {
                    // Persist with null barcode if empty to avoid unique constraint collisions
                    String barcodeToSave = (codigo == null || codigo.isBlank()) ? null : codigo;
                    Product p = Product.builder()
                            .barcode(barcodeToSave)
                            .nombre((descripcion == null || descripcion.isBlank()) ? (barcodeToSave == null ? "" : barcodeToSave) : descripcion)
                            .precio(precioVenta)
                            .precioCompra(precioCosto)
                            .build();

                    // Use repository directly to avoid double history creation in ProductService.create
                    // ProductService.create adds "manual creation" history, but we want to add our own eventName history
                    if (p.getNombre() != null) {
                        p.setNombre(p.getNombre().toUpperCase());
                    }
                    if (p.getBarcode() != null) {
                        p.setBarcode(p.getBarcode().toUpperCase());
                    }
                    if (p.getActivate() == null) {
                        p.setActivate(1);
                    }
                    Product saved = productRepository.save(p);
                    log.info("[IMPORT] Line {}: CREATED product id={} | barcode='{}' | nombre='{}' | precioVenta={} | precioCosto={}", lineNo, saved.getId(), saved.getBarcode(), saved.getNombre(), precioVenta, precioCosto);

                    BigDecimal historialPrecio = toSafeMoney(saved.getPrecio());

                    HistorialProducto hp = HistorialProducto.builder()
                            .productoId(saved.getId())
                            .evento(eventName)
                            .precio(historialPrecio)
                            .activo(true)
                            .build();
                    historialProductoService.create(hp);
                    log.debug("[IMPORT] Line {}: HISTORIAL recorded for productoId={} event='{}' precio={}", lineNo, saved.getId(), eventName, historialPrecio);

                    created++;
                } catch (Exception ex) {
                    errors++;
                    StringBuilder ctx = new StringBuilder();
                    ctx.append("codigo='").append(codigo).append("', ")
                       .append("descripcion='").append(descripcion).append("', ")
                       .append("precioCosto='").append(precioCostoStr).append("', ")
                       .append("precioVenta='").append(precioVentaStr).append("', ")
                       .append("cols=").append(Arrays.toString(cols));
                    String err = ex.getClass().getSimpleName() + ": " + (ex.getMessage() == null ? "(no message)" : ex.getMessage());
                    messages.add("Line " + lineNo + ": error - " + err + " | " + ctx);
                    log.error("[IMPORT] Line {}: ERROR {} | {}", lineNo, err, ctx, ex);
                }
            }
            }
        } catch (Exception e) {
            log.error("[IMPORT] Failed to read CSV file='{}' | event='{}'", filename, eventName, e);
            return MigrationResult.error("Failed to read CSV: " + e.getMessage());
        }

        log.info("[IMPORT] Completed importVentasYa | file='{}' | event='{}' | created={} | skipped={} | duplicates={} | errors={}", filename, eventName, created, skipped, duplicates, errors);
        return new MigrationResult(created, skipped, duplicates, errors, messages);
    }

    @Override
    public MigrationResult importarLite(MultipartFile file, String eventName) {
        if (file == null || file.isEmpty()) {
            return MigrationResult.error("Empty file");
        }
        if (eventName == null || eventName.isBlank()) {
            return MigrationResult.error("Missing eventName");
        }

        String filename = file.getOriginalFilename();
        log.info("[IMPORT] Starting importarLite | event='{}' | file='{}'", eventName, filename);

        MigrationResult result;
        // Check if file is Excel (xlsx)
        if (filename != null && (filename.endsWith(".xlsx") || filename.endsWith(".xls"))) {
            result = importLiteExcel(file, eventName);
        } else {
            // Fallback to CSV/Text processing
            result = importLiteCsv(file, eventName);
        }
        
        return result;
    }

    private MigrationResult importLiteExcel(MultipartFile file, String eventName) {
        int created = 0;
        int skipped = 0;
        int duplicates = 0;
        int errors = 0;
        List<String> messages = new ArrayList<>();
        List<MigrationResult.Conflict> conflictos = new ArrayList<>();
        String filename = file.getOriginalFilename();

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            int lineNo = 0;

            for (Row row : sheet) {
                lineNo++;
                if (lineNo == 1) {
                    // Always skip the first row as it is assumed to be a header
                    log.debug("[IMPORT] Line {}: header row skipped by default", lineNo);
                    continue;
                }
                
                if (row == null) {
                    continue;
                }

                // Fixed 3 columns for Excel: 0=Code, 1=Desc, 2=Price
                // Use RETURN_BLANK_AS_NULL to safely handle missing or blank cells
                String[] cols = new String[3];
                cols[0] = getCellValueAsString(row.getCell(0, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL));
                cols[1] = getCellValueAsString(row.getCell(1, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL));
                cols[2] = getCellValueAsString(row.getCell(2, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL));
                
                // Trim and check for content
                boolean hasContent = false;
                for(int i=0; i<3; i++) {
                    if(cols[i] != null) {
                        cols[i] = cols[i].trim();
                        if(!cols[i].isEmpty()) hasContent = true;
                    } else {
                        cols[i] = "";
                    }
                }
                
                if (!hasContent) {
                     continue;
                }
                
                // Reuse the logic for processing columns
                ImportResult result = processLiteRow(cols, lineNo, eventName);
                
                if (result.status == ImportStatus.CREATED) {
                    created++;
                }
                else if (result.status == ImportStatus.SKIPPED) skipped++;
                else if (result.status == ImportStatus.DUPLICATE) duplicates++;
                else if (result.status == ImportStatus.ERROR) {
                    errors++;
                    messages.add(result.message);
                }
                
                if (result.conflict != null) {
                    conflictos.add(result.conflict);
                }
            }

        } catch (Exception e) {
            log.error("[IMPORT] Failed to read Excel file='{}' | event='{}'", filename, eventName, e);
            return MigrationResult.error("Failed to read Excel: " + e.getMessage());
        }

        log.info("[IMPORT] Completed importarLite (Excel) | file='{}' | event='{}' | created={} | skipped={} | duplicates={} | errors={}", filename, eventName, created, skipped, duplicates, errors);
        return new MigrationResult(created, skipped, duplicates, errors, messages, conflictos);
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // Avoid scientific notation for barcodes
                    double val = cell.getNumericCellValue();
                    if (val == (long) val) {
                        return String.format("%d", (long) val);
                    } else {
                        return String.valueOf(val);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    private MigrationResult importLiteCsv(MultipartFile file, String eventName) {
        int created = 0;
        int skipped = 0;
        int duplicates = 0;
        int errors = 0;
        List<String> messages = new ArrayList<>();
        List<MigrationResult.Conflict> conflictos = new ArrayList<>();
        String filename = file.getOriginalFilename();

        try {
            byte[] bytes = file.getBytes();
            Charset detected = detectCharset(bytes);
            String content = new String(bytes, detected);
            if (log.isInfoEnabled()) {
                log.info("[IMPORT] Using charset '{}' for file='{}'", detected.name(), filename);
            }
            if (!content.isEmpty() && content.charAt(0) == '\uFEFF') {
                content = content.substring(1);
            }

            try (BufferedReader br = new BufferedReader(new StringReader(content))) {
                String line;
                int lineNo = 0;
                while ((line = br.readLine()) != null) {
                    lineNo++;
                    String raw = line.trim();
                    if (raw.isEmpty()) {
                        log.debug("[IMPORT] Line {}: empty/blank, skipped", lineNo);
                        continue;
                    }

                    if (lineNo == 1) {
                        // Always skip the first row as it is assumed to be a header
                        log.debug("[IMPORT] Line {}: header row skipped by default", lineNo);
                        continue;
                    }

                    log.trace("[IMPORT] Line {} RAW: {}", lineNo, raw);

                    String[] cols = splitFlexible(raw);
                    
                    ImportResult result = processLiteRow(cols, lineNo, eventName);

                    if (result.status == ImportStatus.CREATED) {
                        created++;
                    }
                    else if (result.status == ImportStatus.SKIPPED) skipped++;
                    else if (result.status == ImportStatus.DUPLICATE) duplicates++;
                    else if (result.status == ImportStatus.ERROR) {
                        errors++;
                        messages.add(result.message);
                    }
                    
                    if (result.conflict != null) {
                        conflictos.add(result.conflict);
                    }
                }
            }
        } catch (Exception e) {
            log.error("[IMPORT] Failed to read CSV file='{}' | event='{}'", filename, eventName, e);
            return MigrationResult.error("Failed to read CSV: " + e.getMessage());
        }

        log.info("[IMPORT] Completed importarLite (CSV) | file='{}' | event='{}' | created={} | skipped={} | duplicates={} | errors={}", filename, eventName, created, skipped, duplicates, errors);
        return new MigrationResult(created, skipped, duplicates, errors, messages, conflictos);
    }

    private enum ImportStatus { CREATED, SKIPPED, DUPLICATE, ERROR }
    
    private static class ImportResult {
        ImportStatus status;
        String message;
        MigrationResult.Conflict conflict;
        
        ImportResult(ImportStatus status, String message) {
            this.status = status;
            this.message = message;
        }
        
        ImportResult(ImportStatus status, String message, MigrationResult.Conflict conflict) {
            this.status = status;
            this.message = message;
            this.conflict = conflict;
        }
        
        static ImportResult created() { return new ImportResult(ImportStatus.CREATED, null); }
        static ImportResult skipped() { return new ImportResult(ImportStatus.SKIPPED, null); }
        static ImportResult duplicate() { return new ImportResult(ImportStatus.DUPLICATE, null); }
        static ImportResult duplicate(MigrationResult.Conflict conflict) { return new ImportResult(ImportStatus.DUPLICATE, null, conflict); }
        static ImportResult error(String msg) { return new ImportResult(ImportStatus.ERROR, msg); }
        static ImportResult conflict(MigrationResult.Conflict conflict) { return new ImportResult(ImportStatus.SKIPPED, null, conflict); }
    }

    private ImportResult processLiteRow(String[] cols, int lineNo, String eventName) {
        String codigo = null;
        String descripcion = null;
        String precioVentaStr = null;

        // Try to identify columns based on content
        String col0 = safeGet(cols, 0);
        String col1 = safeGet(cols, 1);
        String col2 = safeGet(cols, 2);

        boolean col0IsBarcode = col0 != null && col0.matches(BARCODE_REGEX);
        boolean col1IsBarcode = col1 != null && col1.matches(BARCODE_REGEX);
        
        // Scenario 1: Standard (Barcode | Description | Price) or (Barcode | Price)
        if (col0IsBarcode) {
            codigo = col0;
            if (cols.length == 2) {
                // Barcode | Price
                descripcion = "";
                precioVentaStr = col1;
            } else {
                // Barcode | Description | Price
                // If col1 is description and col2 is price
                if (looksLikeMoney(col2)) {
                     descripcion = col1;
                     precioVentaStr = col2;
                } else {
                    // Maybe description is split or price is further down
                    precioVentaStr = safeGet(cols, cols.length - 1);
                    // Join middle columns for description
                    StringBuilder descBuilder = new StringBuilder();
                    for (int i = 1; i < cols.length - 1; i++) {
                        if (descBuilder.length() > 0) descBuilder.append(" ");
                        descBuilder.append(cols[i]);
                    }
                    descripcion = descBuilder.toString();
                }
            }
        } 
        // Scenario 2: Description | Barcode | Price (Inverted)
        else if (col1IsBarcode) {
             descripcion = col0;
             codigo = col1;
             precioVentaStr = safeGet(cols, 2);
        }
        // Scenario 3: Description | Price (No Barcode or Barcode in description?)
        else {
             // Fallback logic based on user request:
             // "PANBLANDITO PAN BLANDITO 2500" -> PANBLANDITO (code), PAN BLANDITO (desc), 2500 (price)
             // "ALQURIA 200ML 1000" -> ALQURIA 200ML (code), "" (desc), 1000 (price)
             
             // If 3 columns: Code | Desc | Price
             if (cols.length >= 3) {
                 codigo = col0;
                 descripcion = col1;
                 precioVentaStr = col2;
                 // If col2 is not money, maybe look for money at the end
                 if (!looksLikeMoney(precioVentaStr)) {
                     precioVentaStr = safeGet(cols, cols.length - 1);
                 }
             } else if (cols.length == 2) {
                 // Code | Price
                 codigo = col0;
                 descripcion = "";
                 precioVentaStr = col1;
             }
        }

        // Special handling for barcodes starting with "HTTPSÑ--"
        if (codigo != null && codigo.toUpperCase(Locale.ROOT).startsWith("HTTPSÑ--")) {
            log.debug("[IMPORT] Line {}: barcode starts with HTTPSÑ--, clearing barcode. Original='{}'", lineNo, codigo);
            codigo = "";
        }

        if ((codigo == null || codigo.isBlank()) && (descripcion == null || descripcion.isBlank())) {
            log.warn("[IMPORT] Line {}: missing both barcode and description, skipped | cols={}", lineNo, Arrays.toString(cols));
            return ImportResult.skipped();
        }

        Double precioVenta = parseMoneyToDouble(precioVentaStr).orElse(0.0);
        Double precioCosto = 0.0;

        log.debug("[IMPORT] Line {}: parsed codigo='{}' descripcion='{}' precioVenta={}", lineNo, codigo, descripcion, precioVenta);

        // Conflict Check 0: No price (Price is zero)
        if (precioVenta == 0.0) {
            Map<String, String> conflictDetails = new HashMap<>();
            conflictDetails.put("nombre", descripcion);
            conflictDetails.put("codigo", codigo);
            
            List<Map<String, String>> conflictList = new ArrayList<>();
            conflictList.add(conflictDetails);
            
            MigrationResult.Conflict conflict = new MigrationResult.Conflict("No tiene precio", descripcion, conflictList);
            return ImportResult.conflict(conflict);
        }

        // Conflict Check 1: Same name and barcode (Self-check).
        // Per user request, this scenario only applies if both barcode and name are non-null and not blank.
        if (codigo != null && !codigo.isBlank() && descripcion != null && !descripcion.isBlank()) {
            if (codigo.trim().equalsIgnoreCase(descripcion.trim())) {
                Map<String, String> conflictDetails = new HashMap<>();
                conflictDetails.put("nombre", descripcion);
                
                List<Map<String, String>> conflictList = new ArrayList<>();
                conflictList.add(conflictDetails);
                
                String nombreConPrecio = descripcion + "; Precio: " + String.format("$%,.2f", precioVenta);
                MigrationResult.Conflict conflict = new MigrationResult.Conflict("Nombre y codigo de barras iguales", nombreConPrecio, conflictList);
                return ImportResult.conflict(conflict);
            }
        }

        // Conflict Check 2: Same name but different barcode or price (Database check)
        if (descripcion != null && !descripcion.isBlank()) {
             try {
                 org.springframework.data.domain.Page<Product> page = productService.getByName(descripcion, org.springframework.data.domain.PageRequest.of(0, 10));
                 for (Product existing : page.getContent()) {
                     if (existing.getNombre().equalsIgnoreCase(descripcion)) {
                         // Found a product with the same name.
                         // Check if barcode or price is different.
                         boolean barcodeDiff = !Objects.equals(existing.getBarcode(), codigo);
                         // Price check: compare doubles/bigdecimals.
                         // existing.getPrecio() is Double.
                         boolean priceDiff = Math.abs(existing.getPrecio() - precioVenta) > 0.01; // epsilon
                         
                         if (barcodeDiff || priceDiff) {
                             List<Map<String, String>> conflictList = new ArrayList<>();
                             
                             if (barcodeDiff) {
                                 Map<String, String> c = new HashMap<>();
                                 c.put("codigo barras", existing.getBarcode());
                                 c.put("codigo barras 2nd", codigo);
                                 conflictList.add(c);
                             }
                             
                             if (priceDiff) {
                                 Map<String, String> c = new HashMap<>();
                                 c.put("precio", String.format("$%,.2f", existing.getPrecio()));
                                 c.put("precio 2nd", String.format("$%,.2f", precioVenta));
                                 conflictList.add(c);
                             }
                             
                             if (!conflictList.isEmpty()) {
                                 MigrationResult.Conflict conflict = new MigrationResult.Conflict("2 productos con nombres iguales", descripcion, conflictList);
                                 return ImportResult.conflict(conflict);
                             }
                         }
                     }
                 }
             } catch (Exception e) {
                 log.warn("Failed to check for name conflicts", e);
             }
        }

        if (codigo != null && !codigo.isBlank()) {
            String normalizedBarcode = codigo.toUpperCase(Locale.ROOT);
            if (productRepository.findByBarcode(normalizedBarcode).isPresent()) {
                String dupMsg = "Line " + lineNo + ": duplicate barcode '" + codigo + "' – skipped";
                log.warn("[IMPORT] {}", dupMsg);
                return ImportResult.duplicate();
            }
        }

        try {
            String barcodeToSave = (codigo == null || codigo.isBlank()) ? null : codigo;
            // If description is missing, do NOT default to barcode. Leave it empty.
            String nombre = (descripcion == null || descripcion.isBlank()) ? "" : descripcion;

            Product p = Product.builder()
                    .barcode(barcodeToSave)
                    .nombre(nombre)
                    .precio(precioVenta)
                    .precioCompra(precioCosto)
                    .fechaCreacion(LocalDateTime.now())
                    .build();

            // Use repository directly to avoid double history creation in ProductService.create
            // ProductService.create adds "manual creation" history, but we want to add our own eventName history
            if (p.getNombre() != null) {
                p.setNombre(p.getNombre().toUpperCase());
            }
            if (p.getBarcode() != null) {
                p.setBarcode(p.getBarcode().toUpperCase());
            }
            if (p.getActivate() == null) {
                p.setActivate(1);
            }
            Product saved = productRepository.save(p);
            log.info("[IMPORT] Line {}: CREATED product id={} | barcode='{}' | nombre='{}' | precioVenta={}", lineNo, saved.getId(), saved.getBarcode(), saved.getNombre(), precioVenta);

            BigDecimal historialPrecio = toSafeMoney(saved.getPrecio());

            HistorialProducto hp = HistorialProducto.builder()
                    .productoId(saved.getId())
                    .evento(eventName)
                    .precio(historialPrecio)
                    .activo(true)
                    .build();
            historialProductoService.create(hp);
            log.debug("[IMPORT] Line {}: HISTORIAL recorded for productoId={} event='{}' precio={}", lineNo, saved.getId(), eventName, historialPrecio);

            return ImportResult.created();
        } catch (Exception ex) {
            String ctx = "codigo='" + codigo + "', descripcion='" + descripcion + "', precioVenta='" + precioVentaStr + "'";
            String err = ex.getClass().getSimpleName() + ": " + (ex.getMessage() == null ? "(no message)" : ex.getMessage());
            log.error("[IMPORT] Line {}: ERROR {} | {}", lineNo, err, ctx, ex);
            return ImportResult.error("Line " + lineNo + ": error - " + err + " | " + ctx);
        }
    }

    private static boolean looksLikeLiteHeader(String raw) {
        String normalized = raw.toLowerCase(Locale.ROOT);
        return normalized.contains("codigo") && normalized.contains("nombre") && normalized.contains("precio");
    }

    private static BigDecimal toSafeMoney(Double val) {
        if (val == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        // Guard against NaN/Infinity
        if (val.isNaN() || val.isInfinite()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal bd = BigDecimal.valueOf(val).setScale(2, RoundingMode.HALF_UP);
        // DB columns use precision=10, scale=2 -> max absolute 99,999,999.99
        BigDecimal max = new BigDecimal("99999999.99");
        BigDecimal min = max.negate();
        if (bd.compareTo(max) > 0) return max;
        if (bd.compareTo(min) < 0) return min;
        return bd;
    }

    private static boolean looksLikeHeader(String raw) {
        String normalized = raw.toLowerCase(Locale.ROOT);
        return normalized.contains("codigo") && normalized.contains("descripcion");
    }

    private static String[] splitFlexible(String line) {
        // Prefer comma if present; else try tab; else collapse multiple spaces; finally, fallback to any whitespace
        String[] out;
        if (line.contains(",")) {
            out = Arrays.stream(line.split(","))
                    .map(String::trim)
                    .toArray(String[]::new);
        } else if (line.contains("\t")) {
            out = Arrays.stream(line.split("\t+"))
                    .map(String::trim)
                    .toArray(String[]::new);
        } else {
            String[] parts = Arrays.stream(line.split("\\s{2,}")) // 2+ spaces as delimiter
                    .map(String::trim)
                    .toArray(String[]::new);
            if (parts.length <= 1 && line.contains(" ")) {
                // Fallback when data is single-space separated
                String[] fallback = Arrays.stream(line.split("\\s+"))
                        .map(String::trim)
                        .toArray(String[]::new);
                if (fallback.length > parts.length) {
                    if (log.isTraceEnabled()) {
                        log.trace("[IMPORT] splitFlexible fallback to single-space split. Before parts={}, after parts={}", parts.length, fallback.length);
                    }
                    out = fallback;
                } else {
                    out = parts;
                }
            } else {
                out = parts;
            }
        }
        // Expand any embedded tab-separated values inside previously split tokens (e.g., when CSV uses commas but inner columns use tabs)
        out = flattenEmbeddedTabs(out);
        // Normalize case where first token contains "<barcode><space><description>" and the rest columns follow,
        // which typically happens when prices are all $0 and separated by 2+ spaces.
        out = normalizeLeadingBarcodeAndDesc(out);
        return out;
    }

    private static String[] normalizeLeadingBarcodeAndDesc(String[] parts) {
        if (parts == null || parts.length == 0) return parts;
        String first = parts[0];
        if (first == null) return parts;
        // Match: digits (5-30) then at least one space then some text
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("^(\\d{5,30})\\s+(.+)$");
        java.util.regex.Matcher m = p.matcher(first);
        if (m.matches()) {
            String maybeBarcode = m.group(1);
            String maybeDesc = m.group(2).trim();
            // Only split if the supposed barcode matches our global rule
            if (maybeBarcode.matches(BARCODE_REGEX)) {
                String[] rebuilt = new String[parts.length + 1];
                rebuilt[0] = maybeBarcode;
                rebuilt[1] = maybeDesc;
                // Shift the rest by 1 position
                System.arraycopy(parts, 1, rebuilt, 2, parts.length - 1);
                if (log.isDebugEnabled()) {
                    log.debug("[IMPORT] Normalized leading token combining barcode+desc -> barcode='{}' desc='{}'", maybeBarcode, maybeDesc);
                }
                return rebuilt;
            }
        }
        return parts;
    }

    private static String[] flattenEmbeddedTabs(String[] parts) {
        if (parts == null || parts.length == 0) return parts;
        boolean anyTabbed = false;
        List<String> flat = new ArrayList<>();
        for (String token : parts) {
            if (token != null && token.contains("\t")) {
                anyTabbed = true;
                String[] subs = token.split("\t+");
                for (String s : subs) {
                    if (s != null) {
                        String t = s.trim();
                        if (!t.isEmpty()) flat.add(t);
                    }
                }
            } else {
                flat.add(token);
            }
        }
        if (anyTabbed && log.isDebugEnabled()) {
            log.debug("[IMPORT] Expanded embedded tab-separated columns: before parts={} after parts={}", parts.length, flat.size());
        }
        return flat.toArray(new String[0]);
    }

    private static String safeGet(String[] arr, int idx) {
        return idx < arr.length ? arr[idx] : null;
    }

    private static String findPrecioCosto(String[] cols) {
        // Heuristic: first money-like token
        for (String c : cols) {
            if (looksLikeMoney(c)) return c;
        }
        return null;
    }

    private static String findPrecioVenta(String[] cols) {
        // Heuristic: second money-like token
        boolean firstFound = false;
        for (String c : cols) {
            if (looksLikeMoney(c)) {
                if (!firstFound) {
                    firstFound = true; // Costo
                } else {
                    return c; // Venta
                }
            }
        }
        return null;
    }

    private static boolean looksLikeMoney(String s) {
        if (s == null) return false;
        String t = s.trim().toUpperCase(Locale.ROOT);
        if (t.equals("N/A")) return true;
        // Accept formats like $0, $1.700, 1.700, 1500, $35.000
        return t.matches("^\\$?[0-9]{1,3}(\\.[0-9]{3})*(,[0-9]+)?$") || t.matches("^\\$?[0-9]+(,[0-9]+)?$");
    }

    private static Optional<Double> parseMoneyToDouble(String s) {
        if (s == null) return Optional.empty();
        String t = s.trim().toUpperCase(Locale.ROOT);
        if (t.isEmpty() || t.equals("N/A")) return Optional.of(0.0);
        // Remove currency symbols and thousand separators; handle comma decimals if present
        t = t.replace("$", "").replace(" ", "");
        if (t.contains(",")) {
            t = t.replace(".", "");
            t = t.replace(",", ".");
        } else {
            t = t.replace(".", "");
        }
        try {
            return Optional.of(Double.parseDouble(t));
        } catch (NumberFormatException e) {
            return Optional.of(0.0);
        }
    }

    private static Charset detectCharset(byte[] bytes) {
        try {
            if (bytes == null || bytes.length == 0) return StandardCharsets.UTF_8;
            // UTF-8 BOM
            if (bytes.length >= 3 && (bytes[0] & 0xFF) == 0xEF && (bytes[1] & 0xFF) == 0xBB && (bytes[2] & 0xFF) == 0xBF) {
                return StandardCharsets.UTF_8;
            }
            // Candidates: UTF-8, Windows-1252, ISO-8859-1
            Charset win1252 = Charset.forName("Windows-1252");
            Charset iso88591 = Charset.forName("ISO-8859-1");
            List<Charset> candidates = Arrays.asList(StandardCharsets.UTF_8, win1252, iso88591);
            Charset best = StandardCharsets.UTF_8;
            int bestScore = Integer.MAX_VALUE;
            Map<String, Integer> scores = new LinkedHashMap<>();
            for (Charset cs : candidates) {
                String text = new String(bytes, cs);
                int score = scoreTextForEncodingIssues(text);
                scores.put(cs.name(), score);
                if (score < bestScore) {
                    bestScore = score;
                    best = cs;
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("[IMPORT] Charset scores: {} | selected={} (score={})", scores, best.name(), bestScore);
            }
            return best;
        } catch (Exception e) {
            return StandardCharsets.UTF_8;
        }
    }

    private static int scoreTextForEncodingIssues(String text) {
        if (text == null || text.isEmpty()) return 0;
        int score = 0;
        // Replacement character count ()
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\uFFFD') score += 10; // heavy penalty
        }
        // Common mojibake sequences when UTF-8 is decoded as Latin-1
        score += countOccurrences(text, "Ã") * 3;
        score += countOccurrences(text, "Â") * 2;
        score += countOccurrences(text, "") * 5; // literal replacement in some renderings
        return score;
    }

    private static int countOccurrences(String haystack, String needle) {
        if (needle == null || needle.isEmpty() || haystack == null || haystack.isEmpty()) return 0;
        int idx = 0, cnt = 0;
        while ((idx = haystack.indexOf(needle, idx)) != -1) {
            cnt++;
            idx += needle.length();
        }
        return cnt;
    }
}