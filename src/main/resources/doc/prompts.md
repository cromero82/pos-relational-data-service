## logica del MigrationService gestion de conflictos en registros:
```powershel
dentro del objeto MigrationResult Incluye Un campo llamado conflicto Que es un arreglo de datos  Que tendrá Dos campos 1 llamado Campo referencia  Y otro campo Conflicto  Y allí poblarás  los siguientes escenarios De los productos que se han cargado :
Productos con mismo nombre Pero código de barras YO precio distinto  Ejemplo :
codigo barras: WINNY
nombre: WINNY ETAPA 4
precio: $1,800.00
y Este otro registro :
codigo barras: 7701021114136
nombre: WINNY ETAPA 4
precio: $1,600.00

Para este ejemplo El valor Del item conflicto debería ser:
[
{
referencia: "2 productos con nombres iguales",
conflicto[
{codigo barras: "7701021114136", codigo barras 2nd: "WINNY"},
{precio: "$1,800.00", precio 2nd: "$1,600.00"}]
},..
]
El caso anterior Puede que el precio sea igual En tal caso Ese es su registro de precio no no estaría allí Sería omitido

Y otro escenario Es donde código de barras sin nombre son iguales (Esto no aplica para un registro versus otro esto aplica Como un análisis de cada registro en sí ) Para lo cual El registro de conflictos Debería ser
ejemplo datos:
codigo barras:PANELITA
nombre:PANELITA
precio: 1100.00
Con lo que el registro de conflictos es Para este ejemplo
[
{
referencia: "Nombre y codigo de barras iguales",
conflicto[
{nombre: "PANELITA"},
},..
]
```