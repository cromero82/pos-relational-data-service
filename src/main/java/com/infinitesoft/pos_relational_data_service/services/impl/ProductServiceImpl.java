package com.infinitesoft.pos_relational_data_service.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infinitesoft.pos_relational_data_service.entities.HistorialProducto;
import com.infinitesoft.pos_relational_data_service.entities.Product;
import com.infinitesoft.pos_relational_data_service.entities.enums.BitacoraEvento;
import com.infinitesoft.pos_relational_data_service.dto.BitacoraUsuarioRequest;
import com.infinitesoft.pos_relational_data_service.repositories.HistorialProductoRepository;
import com.infinitesoft.pos_relational_data_service.repositories.ProductRepository;
import com.infinitesoft.pos_relational_data_service.services.BitacoraUsuarioService;
import com.infinitesoft.pos_relational_data_service.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Override
    public Page<Product> getAll(String barcodeOrName, Pageable pageable) {
        Pageable sorted = withNameAsc(pageable);
        if (barcodeOrName == null || barcodeOrName.isBlank()) {
            return productRepository.findAll(sorted);
        }

        String query = barcodeOrName.toUpperCase().trim();
        String[] words = query.split("\\s+");

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
    public Page<Product> busquedaSmart(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return productRepository.findAll(withNameAsc(pageable));
        }

        String q = query.trim().toUpperCase();
        Pageable sorted = withNameAsc(pageable);

        // 1. Intento búsqueda normal (completa)
        Page<Product> results = productRepository.searchAllByBarcodeOrNombreContaining(q, sorted);
        if (results.hasContent()) {
            return results;
        }

        String[] words = q.split("\\s+");
        if (words.length > 1) {
            // 2. Lógica de unión de palabras
            // Intentar uniendo palabras progresivamente
            // Ej: "7 up man go"
            // Probar uniones de pares adyacentes, luego tríos, etc.
            // Simplificación según el ejemplo: unir todo o unir de a dos
            
            // Caso 2.1: Unir todo
            String joinedAll = String.join("", words);
            results = productRepository.searchAllByBarcodeOrNombreContaining(joinedAll, sorted);
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
                results = productRepository.searchAllByBarcodeOrNombreContaining(sb.toString(), sorted);
                if (results.hasContent()) return results;
            } else if (words.length == 3) {
                // Caso 3 palabras: "7 up man" -> "7up man" o "7 upman"
                String attempt1 = words[0] + words[1] + " " + words[2];
                results = productRepository.searchAllByBarcodeOrNombreContaining(attempt1, sorted);
                if (results.hasContent()) return results;

                String attempt2 = words[0] + " " + words[1] + words[2];
                results = productRepository.searchAllByBarcodeOrNombreContaining(attempt2, sorted);
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
                List<Product> hits = productRepository.searchAllByBarcodeOrNombreContaining(word, Pageable.unpaged()).getContent();
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
    public Optional<Product> getByBarcode(String barcode) {
        if (barcode == null || barcode.isBlank()) {
            return Optional.empty();
        }
        return productRepository.findByBarcode(barcode.toUpperCase());
    }

    private Pageable withNameAsc(Pageable pageable) {
        Sort sort = Sort.by(Sort.Order.asc("nombre").ignoreCase());
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    @Override
    @Transactional
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

        return saved;
    }

    @Override
    @Transactional
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
        existing.setPrecio(product.getPrecio());
        existing.setPrecioCompra(product.getPrecioCompra());
        // Preserve activate flag unless explicitly provided
        if (product.getActivate() != null) {
            existing.setActivate(product.getActivate());
        }
        Product saved = productRepository.save(existing);

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

        return saved;
    }

    @Override
    @Transactional
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

        return saved;
    }

    @Override
    @Transactional
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

        return saved;
    }

    @Override
    public boolean delete(Long id) {
        if (id == null) return false;
        int updated = productRepository.softDeleteById(id);
        return updated > 0;
    }
}
