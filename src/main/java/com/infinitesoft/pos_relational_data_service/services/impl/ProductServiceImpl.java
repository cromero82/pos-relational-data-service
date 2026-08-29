package com.infinitesoft.pos_relational_data_service.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infinitesoft.pos_relational_data_service.entities.ConfiguracionApp;
import com.infinitesoft.pos_relational_data_service.entities.HistorialProducto;
import com.infinitesoft.pos_relational_data_service.entities.Product;
import com.infinitesoft.pos_relational_data_service.util.DateUtils;
import com.infinitesoft.pos_relational_data_service.entities.enums.BitacoraEvento;
import com.infinitesoft.pos_relational_data_service.dto.BitacoraUsuarioRequest;
import com.infinitesoft.pos_relational_data_service.dto.FilterRequest;
import com.infinitesoft.pos_relational_data_service.repositories.ConfiguracionAppRepository;
import com.infinitesoft.pos_relational_data_service.repositories.HistorialProductoRepository;
import com.infinitesoft.pos_relational_data_service.repositories.ProductRepository;
import com.infinitesoft.pos_relational_data_service.services.BitacoraUsuarioService;
import com.infinitesoft.pos_relational_data_service.services.ProductService;
import com.infinitesoft.pos_relational_data_service.services.ProductoPresentacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private HistorialProductoRepository historialProductoRepository;
    @Autowired
    private BitacoraUsuarioService bitacoraUsuarioService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ConfiguracionAppRepository configuracionAppRepository;
    @Autowired
    private ProductoPresentacionService productoPresentacionService;

    @Override
    public Page<Product> getAll(String barcodeOrName, Pageable pageable) {
        Pageable sorted = withNameAsc(pageable);
        if (barcodeOrName == null || barcodeOrName.isBlank()) {
            return productRepository.findAll(sorted);
        }

        String query = normalizeProductSearchQuery(barcodeOrName);
        String[] words = query.trim().split("\\s+");

        if (words.length < 2) {
            return productRepository.searchActiveByBarcodeOrNombreContaining(query, sorted);
        }

        // Specification for products containing ALL words
        Specification<Product> allWordsSpec = (root, q, cb) -> {
            Predicate[] predicates = Arrays.stream(words)
                    .map(word -> cb.like(cb.upper(root.get("nombre")), "%" + word + "%"))
                    .toArray(Predicate[]::new);
            return cb.and(predicates);
        };

        // Specification for products containing ANY word
        Specification<Product> anyWordSpec = (root, q, cb) -> {
            Predicate[] predicates = Arrays.stream(words)
                    .map(word -> cb.like(cb.upper(root.get("nombre")), "%" + word + "%"))
                    .toArray(Predicate[]::new);
            return cb.or(predicates);
        };

        // Always active
        Specification<Product> activeSpec = (root, q, cb) -> cb.notEqual(root.get("activate"), 0);

        List<Product> intersectionResults = productRepository.findAll(activeSpec.and(allWordsSpec), sorted.getSort());
        List<Product> unionResults = productRepository.findAll(activeSpec.and(anyWordSpec), sorted.getSort());

        // Use a LinkedHashSet to maintain insertion order and remove duplicates
        Set<Product> combinedResults = new LinkedHashSet<>(intersectionResults);
        combinedResults.addAll(unionResults);

        List<Product> finalList = new ArrayList<>(combinedResults);

        // Manual pagination
        int start = (int) sorted.getOffset();
        int end = Math.min((start + sorted.getPageSize()), finalList.size());
        List<Product> pageContent = finalList.subList(start, end);

        return new PageImpl<>(pageContent, sorted, finalList.size());
    }

    @Override
    public Page<Product> busquedaSmart(String query, Pageable pageable, boolean unicamenteActivos) {
        return busquedaSmart(query, pageable, unicamenteActivos, false);
    }

    @Override
    public Page<Product> busquedaSmart(String query, Pageable pageable, boolean unicamenteActivos,
                                       boolean coincidirTodaPalabraIndividual) {
        if (query == null || query.isBlank()) {
            return unicamenteActivos ? productRepository.findAllActive(withNameAsc(pageable)) : productRepository.findAll(withNameAsc(pageable));
        }

        if (coincidirTodaPalabraIndividual) {
            return busquedaCoincidirTodaPalabraIndividual(query, pageable, unicamenteActivos);
        }

        String q = normalizeProductSearchQuery(query);
        Pageable sorted = withNameAsc(pageable);

        // 1. Intento búsqueda normal (completa)
        Page<Product> results = unicamenteActivos ? productRepository.searchActiveByBarcodeOrNombreContaining(q, sorted) : productRepository.searchAllByBarcodeOrNombreContaining(q, sorted);
        if (results.hasContent()) {
            return results;
        }

        String[] words = q.trim().split("\\s+");
        if (words.length > 1) {
            // 2. Lógica de unión de palabras
            // Intentar uniendo palabras progresivamente
            // Ej: "7 up man go"
            // Probar uniones de pares adyacentes, luego tríos, etc.
            // Simplificación según el ejemplo: unir todo o unir de a dos
            
            // Caso 2.1: Unir todo
            String joinedAll = String.join("", words);
            results = unicamenteActivos ? productRepository.searchActiveByBarcodeOrNombreContaining(joinedAll, sorted) : productRepository.searchAllByBarcodeOrNombreContaining(joinedAll, sorted);
            if (results.hasContent()) {
                return results;
            }

            // Caso 2.2: Unir de a dos (ejemplo: "7up mango")
            if (words.length >= 4) {
                // Si hay 4, unimos word0+word1 y word2+word3
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < words.length; i += 2) {
                    if (i + 1 < words.length) {
                        sb.append(words[i]).append(words[i + 1]);
                    } else {
                        sb.append(words[i]);
                    }
                    if (i + 2 < words.length) sb.append(" ");
                }
                results = unicamenteActivos ? productRepository.searchActiveByBarcodeOrNombreContaining(sb.toString(), sorted) : productRepository.searchAllByBarcodeOrNombreContaining(sb.toString(), sorted);
                if (results.hasContent()) return results;
            } else if (words.length == 3) {
                // Caso 3 palabras: "7 up man" -> "7up man" o "7 upman"
                String attempt1 = words[0] + words[1] + " " + words[2];
                results = unicamenteActivos ? productRepository.searchActiveByBarcodeOrNombreContaining(attempt1, sorted) : productRepository.searchAllByBarcodeOrNombreContaining(attempt1, sorted);
                if (results.hasContent()) return results;

                String attempt2 = words[0] + " " + words[1] + words[2];
                results = unicamenteActivos ? productRepository.searchActiveByBarcodeOrNombreContaining(attempt2, sorted) : productRepository.searchAllByBarcodeOrNombreContaining(attempt2, sorted);
                if (results.hasContent()) return results;
            }
        }

        // 3. Lógica de palabras desordenadas y búsqueda individual
        if (words.length > 1) {
            List<Product> combinedResults = new ArrayList<>();
            Map<Long, Product> productMap = new LinkedHashMap<>();
            
            // Buscar cada palabra individualmente (pero solo las que tienen longitud razonable para evitar ruidos)
            List<Set<Long>> wordHits = new ArrayList<>();
            for (String word : words) {
                if (word.length() < 2) continue;
                List<Product> hits = unicamenteActivos ? productRepository.searchActiveByBarcodeOrNombreContaining(word, Pageable.unpaged()).getContent() : productRepository.searchAllByBarcodeOrNombreContaining(word, Pageable.unpaged()).getContent();
                Set<Long> ids = new HashSet<>();
                for (Product p : hits) {
                    ids.add(p.getId());
                    productMap.putIfAbsent(p.getId(), p);
                }
                wordHits.add(ids);
            }

            if (!wordHits.isEmpty()) {
                // Intersección (productos que tienen TODAS las palabras)
                Set<Long> allMatch = new HashSet<>(wordHits.get(0));
                for (int i = 1; i < wordHits.size(); i++) {
                    allMatch.retainAll(wordHits.get(i));
                }

                List<Product> sortedResultList = new ArrayList<>();
                // 1. Los que tienen todas
                List<Product> allMatchList = new ArrayList<>();
                for (Long id : allMatch) {
                    allMatchList.add(productMap.get(id));
                }
                allMatchList.sort(Comparator.comparing(Product::getNombre, String.CASE_INSENSITIVE_ORDER));
                sortedResultList.addAll(allMatchList);

                // 2. Los que tienen la primera palabra (y no están en allMatch)
                Set<Long> firstWordHits = wordHits.get(0);
                List<Product> onlyFirst = new ArrayList<>();
                for (Long id : firstWordHits) {
                    if (!allMatch.contains(id)) {
                        onlyFirst.add(productMap.get(id));
                    }
                }
                onlyFirst.sort(Comparator.comparing(Product::getNombre, String.CASE_INSENSITIVE_ORDER));
                sortedResultList.addAll(onlyFirst);

                // 3. Los que tienen las demás palabras (en orden)
                for (int i = 1; i < wordHits.size(); i++) {
                    Set<Long> currentHits = wordHits.get(i);
                    List<Product> others = new ArrayList<>();
                    for (Long id : currentHits) {
                        if (!allMatch.contains(id) && !firstWordHits.contains(id)) {
                            // Deberíamos también evitar duplicados de palabras anteriores si hubiera más de 2
                            boolean alreadyAdded = false;
                            for(int j=0; j<i; j++) {
                                if(wordHits.get(j).contains(id)) { alreadyAdded = true; break; }
                            }
                            if(!alreadyAdded) others.add(productMap.get(id));
                        }
                    }
                    others.sort(Comparator.comparing(Product::getNombre, String.CASE_INSENSITIVE_ORDER));
                    sortedResultList.addAll(others);
                }

                // Paginación manual de la lista combinada
                int start = (int) pageable.getOffset();
                int end = Math.min((start + pageable.getPageSize()), sortedResultList.size());
                if (start > sortedResultList.size()) {
                    return new PageImpl<>(Collections.emptyList(), pageable, sortedResultList.size());
                }
                return new PageImpl<>(sortedResultList.subList(start, end), pageable, sortedResultList.size());
            }
        }

        return results;
    }

    @Override
    public Page<Product> busquedaPorFiltros(List<FilterRequest> filtros, Pageable pageable, String campoOrdenamiento, String orden, String query) {
        Pageable pageableWithSort = withDynamicSort(pageable, campoOrdenamiento, orden);
        Specification<Product> spec = (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            if (query != null && !query.isBlank()) {
                String q = normalizeProductSearchQuery(query);
                String[] words = q.trim().split("\\s+");

                Predicate smartPredicate;

                if (words.length > 1) {
                    // All words must be present in the name
                    Predicate[] wordPredicates = Arrays.stream(words)
                            .map(word -> cb.like(cb.upper(root.get("nombre")), "%" + word + "%"))
                            .toArray(Predicate[]::new);
                    Predicate allWordsInName = cb.and(wordPredicates);

                    // Or the whole query is in barcode
                    Predicate barcodeMatch = cb.like(cb.upper(root.get("barcode")), "%" + q + "%");

                    smartPredicate = cb.or(allWordsInName, barcodeMatch);

                } else {
                    // Simple case for one word or direct barcode scan
                    Predicate barcodePredicate = cb.like(cb.upper(root.get("barcode")), "%" + q + "%");
                    Predicate nombrePredicate = cb.like(cb.upper(root.get("nombre")), "%" + q + "%");
                    smartPredicate = cb.or(barcodePredicate, nombrePredicate);
                }
                predicates.add(smartPredicate);
            }

            for (FilterRequest filtro : filtros) {
                String campo = filtro.getCampo();
                String condicion = filtro.getCondicion();
                String valor = filtro.getValor();

                // Handle "is null" and "is not null"
                if ("is".equalsIgnoreCase(condicion)) {
                    if ("null".equalsIgnoreCase(valor)) {
                        predicates.add(cb.isNull(root.get(campo)));
                        continue;
                    }
                    if ("not null".equalsIgnoreCase(valor)) {
                        predicates.add(cb.isNotNull(root.get(campo)));
                        continue;
                    }
                }

                // Handle legacy null checks
                if (valor == null || "null".equalsIgnoreCase(valor)) {
                    if ("=".equals(condicion)) {
                        predicates.add(cb.isNull(root.get(campo)));
                        continue;
                    }
                    if ("!=".equals(condicion) || "<>".equals(condicion)) {
                        predicates.add(cb.isNotNull(root.get(campo)));
                        continue;
                    }
                }

                // If we reach here, 'valor' is a real value that needs parsing.
                if (valor == null) {
                    continue; // Should not happen if logic above is correct, but as a safeguard.
                }

                // Determinar el tipo del campo para conversión
                Class<?> fieldType = root.get(campo).getJavaType();

                if (fieldType.equals(LocalDateTime.class) || fieldType.equals(LocalDate.class)) {
                    predicates.add(cb.isNotNull(root.get(campo))); // Exclude nulls for date comparisons
                    if (fieldType.equals(LocalDateTime.class)) {
                        LocalDateTime dateTimeValor = LocalDate.parse(valor, formatter).atStartOfDay();
                        switch (condicion) {
                            case "=":
                                predicates.add(cb.between(root.get(campo), dateTimeValor, dateTimeValor.plusDays(1).minusNanos(1)));
                                break;
                            case ">":
                                predicates.add(cb.greaterThan(root.get(campo), dateTimeValor.plusDays(1).minusNanos(1)));
                                break;
                            case ">=":
                                predicates.add(cb.greaterThanOrEqualTo(root.get(campo), dateTimeValor));
                                break;
                            case "<":
                                predicates.add(cb.lessThan(root.get(campo), dateTimeValor));
                                break;
                            case "<=":
                                predicates.add(cb.lessThanOrEqualTo(root.get(campo), dateTimeValor.plusDays(1).minusNanos(1)));
                                break;
                        }
                    } else { // LocalDate
                        LocalDate dateValor = LocalDate.parse(valor, formatter);
                        switch (condicion) {
                            case "=":
                                predicates.add(cb.equal(root.get(campo), dateValor));
                                break;
                            case ">":
                                predicates.add(cb.greaterThan(root.get(campo), dateValor));
                                break;
                            case ">=":
                                predicates.add(cb.greaterThanOrEqualTo(root.get(campo), dateValor));
                                break;
                            case "<":
                                predicates.add(cb.lessThan(root.get(campo), dateValor));
                                break;
                            case "<=":
                                predicates.add(cb.lessThanOrEqualTo(root.get(campo), dateValor));
                                break;
                        }
                    }
                } else if (Number.class.isAssignableFrom(fieldType) || fieldType.equals(double.class) || fieldType.equals(int.class) || fieldType.equals(long.class)) {
                    Double doubleValor = Double.parseDouble(valor);
                    switch (condicion) {
                        case "=":
                            predicates.add(cb.equal(root.get(campo), doubleValor));
                            break;
                        case ">":
                            predicates.add(cb.greaterThan(root.get(campo), doubleValor));
                            break;
                        case ">=":
                            predicates.add(cb.greaterThanOrEqualTo(root.get(campo), doubleValor));
                            break;
                        case "<":
                            predicates.add(cb.lessThan(root.get(campo), doubleValor));
                            break;
                        case "<=":
                            predicates.add(cb.lessThanOrEqualTo(root.get(campo), doubleValor));
                            break;
                    }
                } else {
                    // Por defecto tratar como String
                    switch (condicion) {
                        case "=":
                            predicates.add(cb.equal(root.get(campo), valor));
                            break;
                        case "like":
                            predicates.add(cb.like(cb.upper(root.get(campo)), "%" + valor.toUpperCase() + "%"));
                            break;
                    }
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return productRepository.findAll(spec, pageableWithSort);
    }

    @Override
    public Page<Product> search(String query, Pageable pageable) {
        Pageable sorted = withNameAsc(pageable);
        if (query == null || query.isEmpty()) {
            return productRepository.findAll(sorted);
        }
        String q = query.toUpperCase();
        return productRepository.searchAllByBarcodeOrNombreContaining(q, sorted);
    }

    @Override
    public Page<Product> getByName(String name, Pageable pageable) {
        Pageable sorted = withNameAsc(pageable);
        if (name == null || name.isEmpty()) {
            return productRepository.findAllActive(sorted);
        }
        String n = name.toUpperCase();
        return productRepository.findActiveByNombreContaining(n, sorted);
    }

    @Override
    @Cacheable(value = "products", key = "#barcode == null ? null : #barcode.toUpperCase()", unless = "#barcode == null")
    public Optional<Product> getByBarcode(String barcode) {
        if (barcode == null || barcode.isBlank()) {
            return Optional.empty();
        }
        return productRepository.findByBarcode(barcode.toUpperCase());
    }

    @Override
    @CacheEvict(value = "products", key = "#barcode == null ? null : #barcode.toUpperCase()", condition = "#barcode != null && !#barcode.isBlank()")
    public void evictProductCacheByBarcode(String barcode) {
        // La anotación invalida la entrada en caché por código de barras
    }

    @Override
    public Optional<Product> getById(Long id) {
        Optional<Product> opt = productRepository.findById(id);
        opt.ifPresent(p -> {
            productoPresentacionService.ensureAndSyncFromProducto(p.getId());
            p.setPresentaciones(productoPresentacionService.listByProductoId(p.getId(), true));
        });
        return opt;
    }

    private Pageable withDynamicSort(Pageable pageable, String campoOrdenamiento, String orden) {
        String field = (campoOrdenamiento == null || campoOrdenamiento.isBlank()) ? "nombre" : campoOrdenamiento;
        Sort.Direction direction = "desc".equalsIgnoreCase(orden) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, field);
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    private Pageable withNameAsc(Pageable pageable) {
        Sort sort = Sort.by(Sort.Order.asc("nombre"));
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    @Override
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public Product create(Product product) {
        // Normalize fields safely (allow null barcode/nombre)
        if (product.getNombre() != null) {
            product.setNombre(product.getNombre().toUpperCase());
        }
        if (product.getBarcode() != null) {
            product.setBarcode(product.getBarcode().toUpperCase());
        }
        // Ensure activate defaults to 1 (active)
        if (product.getActivate() == null) {
            product.setActivate(1);
        }
        Product saved = productRepository.save(product);
        // Register historical record for manual creation
        HistorialProducto historial = HistorialProducto.builder()
                .productoId(saved.getId())
                .evento("manual creation")
                .precio(saved.getPrecio() != null ? BigDecimal.valueOf(saved.getPrecio()) : null)
                .activo(true)
                .build();
        historialProductoRepository.save(historial);

        // Registrar en bitácora de usuario
        try {
            BitacoraUsuarioRequest bitacoraRequest = new BitacoraUsuarioRequest();
            bitacoraRequest.setEvento(BitacoraEvento.REG_PROD.getSigla());
            bitacoraRequest.setValorDespues(objectMapper.writeValueAsString(saved));
            bitacoraUsuarioService.save(bitacoraRequest);
        } catch (Exception e) {
            // No bloqueamos la creación del producto si falla el registro en bitácora
        }

        actualizarConfiguracionApp(true);

        productoPresentacionService.ensureAndSyncFromProducto(saved.getId());
        saved.setPresentaciones(productoPresentacionService.listByProductoId(saved.getId(), true));
        return saved;
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public Product update(Long id, Product product) {
        if (id == null) {
            return null;
        }
        Optional<Product> existingOpt = productRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return null;
        }
        Product existing = existingOpt.get();

        String valorAntes = null;
        try {
            valorAntes = objectMapper.writeValueAsString(existing);
        } catch (Exception e) {
            // Error serializando el estado anterior
        }

        boolean activateChanged = false;

        boolean preciosModificados = !Objects.equals(product.getPrecio(), existing.getPrecio())
                || !Objects.equals(product.getPrecioCompra(), existing.getPrecioCompra())
                || !Objects.equals(product.getPrecioUnidad(), existing.getPrecioUnidad());

        // Full replace (PUT semantics), but keep primary key (id) from path
        if (product.getBarcode() != null) {
            existing.setBarcode(product.getBarcode().toUpperCase());
        } else {
            existing.setBarcode(null);
        }
        if (product.getNombre() != null) {
            existing.setNombre(product.getNombre().toUpperCase());
        } else {
            existing.setNombre(null);
        }
        if (product.getPrecio() != null && !product.getPrecio().equals(existing.getPrecio())) {
            existing.setFechaUltimaActualizacionPrecio(LocalDateTime.now());
        }
        existing.setPrecio(product.getPrecio());
        existing.setPrecioCompra(product.getPrecioCompra());
        existing.setPrecioUnidad(product.getPrecioUnidad());
        // Preserve activate flag unless explicitly provided
        if (product.getActivate() != null) {
            if (!product.getActivate().equals(existing.getActivate())) {
                activateChanged = true;
            }
            existing.setActivate(product.getActivate());
        }
        Product saved = productRepository.save(existing);

        // Actualizar porcentaje_ganancia si el producto tiene precio y precio_compra
        if (saved.getPrecio() != null && saved.getPrecioCompra() != null && saved.getPrecioCompra() > 0) {
            short porcentaje = (short) Math.round(((saved.getPrecio() - saved.getPrecioCompra()) / saved.getPrecioCompra()) * 100);
            productRepository.actualizarPorcentajeGanancia(saved.getId(), porcentaje);
        }

        // Registrar en bitácora de usuario
        try {
            BitacoraUsuarioRequest bitacoraRequest = new BitacoraUsuarioRequest();
            bitacoraRequest.setEvento(BitacoraEvento.MOD_PROD.getSigla());
            bitacoraRequest.setValorAntes(valorAntes);
            bitacoraRequest.setValorDespues(objectMapper.writeValueAsString(saved));
            bitacoraUsuarioService.save(bitacoraRequest);
        } catch (Exception e) {
            // No bloqueamos la actualización del producto si falla el registro en bitácora
        }

        if (activateChanged) {
            actualizarConfiguracionApp(false);
        }

        if (preciosModificados && saved.getGrupoEspejo() != null) {
            propagarPreciosGrupoEspejo(saved);
        }

        productoPresentacionService.ensureAndSyncFromProducto(saved.getId());
        if (preciosModificados && saved.getGrupoEspejo() != null) {
            for (Product p : productRepository.findByGrupoEspejoId(saved.getGrupoEspejo().getId())) {
                productoPresentacionService.ensureAndSyncFromProducto(p.getId());
            }
        }
        saved.setPresentaciones(productoPresentacionService.listByProductoId(saved.getId(), true));
        return saved;
    }

    private void propagarPreciosGrupoEspejo(Product referencia) {
        Long grupoId = referencia.getGrupoEspejo().getId();
        List<Product> productosGrupo = productRepository.findByGrupoEspejoId(grupoId);

        for (Product p : productosGrupo) {
            if (p.getId().equals(referencia.getId())) {
                continue;
            }
            boolean changed = false;
            if (!Objects.equals(p.getPrecio(), referencia.getPrecio())) {
                p.setPrecio(referencia.getPrecio());
                p.setFechaUltimaActualizacionPrecio(DateUtils.obtenerFechaSistema());
                changed = true;
            }
            if (!Objects.equals(p.getPrecioCompra(), referencia.getPrecioCompra())) {
                p.setPrecioCompra(referencia.getPrecioCompra());
                changed = true;
            }
            if (!Objects.equals(p.getPrecioUnidad(), referencia.getPrecioUnidad())) {
                p.setPrecioUnidad(referencia.getPrecioUnidad());
                changed = true;
            }
            if (changed) {
                productRepository.save(p);
                if (p.getPrecio() != null && p.getPrecioCompra() != null && p.getPrecioCompra() > 0) {
                    short porcentaje = (short) Math.round(((p.getPrecio() - p.getPrecioCompra()) / p.getPrecioCompra()) * 100);
                    productRepository.actualizarPorcentajeGanancia(p.getId(), porcentaje);
                }
            }
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public Product deactivate(Long id) {
        if (id == null) {
            return null;
        }
        Optional<Product> existingOpt = productRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return null;
        }
        Product existing = existingOpt.get();

        String valorAntes = null;
        try {
            valorAntes = objectMapper.writeValueAsString(existing);
        } catch (Exception e) {
            // Error serializando el estado anterior
        }

        existing.setActivate(0);
        Product saved = productRepository.save(existing);

        // Registrar en bitácora de usuario
        try {
            BitacoraUsuarioRequest bitacoraRequest = new BitacoraUsuarioRequest();
            bitacoraRequest.setEvento(BitacoraEvento.DESHAB_PROD.getSigla());
            bitacoraRequest.setValorAntes(valorAntes);
            bitacoraRequest.setValorDespues(objectMapper.writeValueAsString(saved));
            bitacoraUsuarioService.save(bitacoraRequest);
        } catch (Exception e) {
            // No bloqueamos si falla el registro en bitácora
        }

        actualizarConfiguracionApp(false);

        return saved;
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public Product activate(Long id) {
        if (id == null) {
            return null;
        }
        Optional<Product> existingOpt = productRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return null;
        }
        Product existing = existingOpt.get();

        String valorAntes = null;
        try {
            valorAntes = objectMapper.writeValueAsString(existing);
        } catch (Exception e) {
            // Error serializando el estado anterior
        }

        existing.setActivate(1);
        Product saved = productRepository.save(existing);

        // Registrar en bitácora de usuario
        try {
            BitacoraUsuarioRequest bitacoraRequest = new BitacoraUsuarioRequest();
            bitacoraRequest.setEvento(BitacoraEvento.HAB_PROD.getSigla());
            bitacoraRequest.setValorAntes(valorAntes);
            bitacoraRequest.setValorDespues(objectMapper.writeValueAsString(saved));
            bitacoraUsuarioService.save(bitacoraRequest);
        } catch (Exception e) {
            // No bloqueamos si falla el registro en bitácora
        }

        actualizarConfiguracionApp(false);

        return saved;
    }

    @Override
    @CacheEvict(value = "products", allEntries = true)
    public boolean delete(Long id) {
        if (id == null) return false;
        int updated = productRepository.softDeleteById(id);
        boolean result = updated > 0;
        if (result) {
            actualizarConfiguracionApp(false);
        }
        return result;
    }

    private void actualizarConfiguracionApp(boolean actualizarTotalProductos) {
        try {
            if (actualizarTotalProductos) {
                long totalProductos = productRepository.count();
                Optional<ConfiguracionApp> configTotal = configuracionAppRepository.findByKey("total-productos");
                if (configTotal.isPresent()) {
                    ConfiguracionApp config = configTotal.get();
                    config.setValue(String.valueOf(totalProductos));
                    configuracionAppRepository.save(config);
                } else {
                    ConfiguracionApp config = ConfiguracionApp.builder()
                            .key("total-productos")
                            .value(String.valueOf(totalProductos))
                            .build();
                    configuracionAppRepository.save(config);
                }
            }

            long totalProductosActivos = productRepository.countByActivateNot(0);
            Optional<ConfiguracionApp> configActivos = configuracionAppRepository.findByKey("total-productos-activos");
            if (configActivos.isPresent()) {
                ConfiguracionApp config = configActivos.get();
                config.setValue(String.valueOf(totalProductosActivos));
                configuracionAppRepository.save(config);
            } else {
                ConfiguracionApp config = ConfiguracionApp.builder()
                        .key("total-productos-activos")
                        .value(String.valueOf(totalProductosActivos))
                        .build();
                configuracionAppRepository.save(config);
            }
        } catch (Exception e) {
            // Log error or handle silently to not disrupt main flow
            e.printStackTrace();
        }
    }

    /**
     * Colapsa 2+ espacios seguidos a uno solo y pasa a mayúsculas.
     * No hace trim: un espacio simple al inicio/fin se conserva (p. ej. "ron " → "RON ").
     */
    private static String normalizeProductSearchQuery(String query) {
        if (query == null) {
            return null;
        }
        return query.replaceAll("\\s{2,}", " ").toUpperCase();
    }

    /**
     * Cada palabra del query debe coincidir como palabra completa en nombre o barcode
     * (p. ej. "RON" no coincide con "APRONAX" ni "CORONA").
     */
    private Page<Product> busquedaCoincidirTodaPalabraIndividual(String query, Pageable pageable,
                                                                boolean unicamenteActivos) {
        String normalized = normalizeProductSearchQuery(query);
        String[] words = normalized == null ? new String[0] : normalized.trim().split("\\s+");
        List<String> significant = new ArrayList<>();
        for (String word : words) {
            if (word != null && !word.isBlank()) {
                significant.add(word);
            }
        }
        if (significant.isEmpty()) {
            return unicamenteActivos
                    ? productRepository.findAllActive(withNameAsc(pageable))
                    : productRepository.findAll(withNameAsc(pageable));
        }

        Pageable sorted = withNameAsc(pageable);
        Specification<Product> spec = (root, criteriaQuery, cb) -> {
            List<Predicate> andWords = new ArrayList<>();
            for (String word : significant) {
                andWords.add(cb.or(
                        wholeWordLike(cb, root.get("nombre"), word),
                        wholeWordLike(cb, root.get("barcode"), word)
                ));
            }
            Predicate wordsMatch = cb.and(andWords.toArray(new Predicate[0]));
            if (unicamenteActivos) {
                return cb.and(cb.notEqual(root.get("activate"), 0), wordsMatch);
            }
            return wordsMatch;
        };
        return productRepository.findAll(spec, sorted);
    }

    /** Coincide palabra completa delimitada por espacios o extremos del texto. */
    private static Predicate wholeWordLike(javax.persistence.criteria.CriteriaBuilder cb,
                                           javax.persistence.criteria.Path<String> field,
                                           String word) {
        String w = escapeLikeMeta(word);
        javax.persistence.criteria.Expression<String> upper = cb.upper(field);
        char esc = '\\';
        return cb.or(
                cb.equal(upper, w),
                cb.like(upper, w + " %", esc),
                cb.like(upper, "% " + w + " %", esc),
                cb.like(upper, "% " + w, esc)
        );
    }

    private static String escapeLikeMeta(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}