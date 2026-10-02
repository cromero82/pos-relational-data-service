# Cómo ejecutar pruebas — POS Infinito (BE + FE)

Guía para QA. No necesitas leer código: copia los comandos y mírame el resultado.

> Estado a 2026-09-30: `mvn test` → **10/10 verde**, `npm run test:ci` → **5/5 verde**.

---

## 1. Backend — `pos-relational-data-service`

Ruta: `C:\dev\repos\pos-relational-data-service`

```bash
# Suite completa
mvn test

# Solo una clase
mvn test -Dtest=CorteVentaServiceImplTest

# Solo un método
mvn test -Dtest=CorteVentaServiceImplTest#eliminarUltimoCorteHaceSoftDelete

# Solo tests unitarios (rápido, sin levantar Spring)
mvn test -Dtest=CorteVentaServiceImplTest -DfailIfNoTests=false
```

**Cómo leer el resultado:**

| Salida | Significa |
|---|---|
| `Tests run: 10, Failures: 0, Errors: 0, Skipped: 0` | Todo bien |
| `BUILD SUCCESS` | Compiló y pasó |
| `BUILD FAILURE` + `Tests run: N, Failures: M` | M tests fallaron |
| `COMPILATION ERROR` | El código ni compiló — no es un fallo de tests |

Detalle por test en `target/surefire-reports/`.

**Importante:** los tests corren contra **H2 en memoria**, no contra PostgreSQL. Un test verde
no garantiza que el SQL funcione en la BD real: H2 no reproduce `jsonb`, `SERIAL` ni las
funciones nativas de Postgres que usa este proyecto.

Todavía **no** hay una separación entre tests unitarios y tests contra la BD real
(eso requiere `@Tag("integration")` + configuración propia, pendiente de implementar).
Por ahora toda la suite usa H2. Cuando se implemente, el comando sería:

```bash
mvn test -Dgroups=integration   # requiere PostgreSQL arriba en localhost:5432
```

---

## 2. Frontend — `infinito-ai-front`

Ruta: `C:\dev\repos\infinito-ai-front`

```bash
# Suite en headless, una sola pasada, sin ventana de Chrome (para QA / automatizar)
npm run test:ci

# Suite normal: abre Chrome y se queda escuchando cambios
npm test

# Con reporte de cobertura
npm run test:coverage
```

**Cómo leer el resultado:**

| Salida | Significa |
|---|---|
| `TOTAL: 5 SUCCESS` | Todo bien |
| `TOTAL: 4 FAILED, 1 SUCCESS` | 4 tests fallaron — abajo dice cuál y por qué |
| `ChromeHeadless ... Executed 0 of 5` | 0 tests corrieron (config rota) |
| `Cannot find module '...'` | Falta una dependencia |

En PowerShell, si `ng test` no encuentra Chrome, defínelo primero:

```powershell
$env:CHROME_BIN = "C:\Program Files\Google\Chrome\Application\chrome.exe"
npm run test:ci
```

Los errores `Error retrieving icon mat:xxx` que aparecen durante la corrida son **ruido conocido**:
el registro de íconos de Material no carga en el entorno de pruebas. No afectan el resultado.

---

## 3. Cobertura actual (línea base honesta)

| Capa | Cobertura | Nota |
|---|---|---|
| BE — servicios de corte/egreso/orígenes | 4 clases, 10 tests | Lo único cubierto |
| BE — controllers (≈100 endpoints) | **0** | Sin `MockMvc` |
| BE — queries nativas Postgres | **0** | H2 no reproduce `jsonb` |
| FE — componentes | 2 specs (productos) | Sobre ~450 archivos `.ts` |
| FE — e2e / flujo de usuario | **0** | No existe Playwright ni Cypress |

**Conclusión: cuando automatizamos un escenario, casi siempre es la primera vez que se prueba
por código.** Es esperado que aparezcan fallos — eso es exactamente el valor: un bug que hoy
solo encontrarías probando a mano queda protegido para siempre.

---

## 4. Flujo de trabajo acordado

1. **QA describe el escenario** en lenguaje natural: qué hizo, qué esperaba, qué obtuvo.
2. Se traduce a un test. Si el resultado fue correcto, el test **protege** ese comportamiento.
   Si fue incorrecto, el test **falla** y documenta el bug.
3. Se ejecuta con `mvn test` / `npm run test:ci`.
4. Si falla por un bug real del código → se reporta, no se "arregla el test para que pase".
5. Si falla porque el test está mal escrito → se corrige el test.

> Regla de oro: **un test que documenta un comportamiento correcto nunca se cambia para que pase.**
> Si el negocio cambió, es una decisión de producto, no un fix de test.

---

## 5. Qué NO automatizar

No todo lo que se prueba a mano es candidato. Manualmente sigue siendo mejor:

- Flujos que dependen de leer una pantalla de pago de un banco real.
- Impresión de tickets y papel (POS físico).
- Comportamiento de lectora de código de barras HID (ver spec `lectora-codigo-barras`, **aparcado**).
- Anulación de tickets por sus consecuencias legales (trazabilidad DIAN).

Esos van a `doc-ayuda-inteligente/CONTEXTO-TESTER-POS.md` como casos manuales, no a código.
