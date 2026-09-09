# Fotos de productos

## Decisión de diseño: se guardan URLs, no archivos binarios

La API **almacena la dirección (URL) de una imagen ya alojada en un
servicio externo**. No recibe, no guarda y no sirve archivos binarios.

En la base de datos, la tabla `fotos` tiene una columna `url` de tipo
`VARCHAR(500)`. No hay ninguna columna `BLOB`, ni carpeta de subidas, ni
endpoint que acepte `multipart/form-data`.

**Consecuencia práctica:** el cliente sube la imagen a donde quiera
(Cloudinary, S3, Imgur, un hosting propio) y recién después le manda a
esta API la URL resultante. Si el servicio externo borra la imagen, acá
queda una URL rota: la API no valida que la imagen exista, solo que la
dirección esté bien formada.

### Qué haría falta para aceptar archivos

Si la consigna exigiera subir archivos en lugar de URLs, el cambio no es
menor. Habría que:

1. Agregar un endpoint que reciba `multipart/form-data`
   (`@RequestParam MultipartFile`).
2. Definir dónde se guarda el binario: disco del servidor, un bucket
   externo, o una columna `@Lob` en MySQL (esta última desaconsejada,
   hace crecer la base y complica los backups).
3. Validar tipo de contenido real y tamaño máximo del archivo.
4. Publicar un endpoint que devuelva el binario, o generar URLs firmadas.

Mientras tanto, esta implementación cubre el requisito de "adjuntar
fotos" tratándolas como referencias.

## Modelo y relación JPA

```
Producto  1 ──────< N  Foto
```

- `Producto.fotos`: `@OneToMany(mappedBy = "producto", cascade = ALL,
  orphanRemoval = true, fetch = LAZY)`. La lista arranca inicializada,
  así nunca hay que chequear `null`.
- `Foto.producto`: `@ManyToOne(fetch = LAZY)` sobre la columna
  `producto_id`, que es `NOT NULL`.
- `cascade = ALL` guarda las fotos junto con el producto;
  `orphanRemoval = true` borra de la base la foto que se saca de la
  lista, sin necesidad de un repositorio propio para `Foto`.
- `Producto.agregarFoto()` y `Producto.eliminarFoto()` mantienen
  sincronizados los dos lados de la relación. Conviene usarlos siempre
  en lugar de tocar la lista directamente.
- Las consultas de `ProductoRepository` usan
  `@EntityGraph(attributePaths = "fotos")` para traer las fotos en la
  misma consulta. Sin eso, listar N productos disparaba N+1 consultas.

`Foto` no tiene repositorio propio: es parte del agregado `Producto` y
siempre se accede navegando desde él. Eso garantiza, además, que no se
pueda borrar por id una foto que pertenece a otro producto.

## Validaciones

Se aplican tanto al crear y actualizar un producto como al agregar fotos
sueltas. Todas devuelven **400** con el mensaje correspondiente:

| Regla | Mensaje |
|---|---|
| La lista no puede venir vacía ni nula | `El producto debe tener al menos una foto` |
| Ninguna URL puede estar en blanco | `La URL de la foto es obligatoria` |
| Debe ser una URL absoluta `http`/`https` con host | `La URL de la foto no es válida: ...` |
| No puede superar los 500 caracteres | `La URL de la foto no puede superar los 500 caracteres` |
| No se puede repetir una URL en el mismo pedido | `La foto ... está repetida` |

Las URLs se guardan con `trim()` aplicado.

Un producto **siempre** tiene al menos una foto: no se puede crear sin
fotos, ni borrar la última que le queda.

## Endpoints

Las fotos se pueden manejar de dos maneras: junto con el producto
(`POST` y `PUT` de `/api/productos`), o de a una con los endpoints
dedicados.

### Agregar fotos a un producto existente

```
POST /api/productos/{id}/fotos
```

```json
{
  "usuarioId": 10,
  "fotos": [
    "https://cdn.ejemplo.com/remera-espalda.jpg"
  ]
}
```

Suma las fotos a las que el producto ya tiene, sin tocar el resto de los
datos. Responde **201** con el producto actualizado.

- **403** si el usuario no es el dueño del producto.
- **404** si el producto no existe.
- **409** si alguna de las URLs ya está cargada en ese producto.

### Eliminar una foto puntual

```
DELETE /api/productos/{id}/fotos/{fotoId}?usuarioId=10
```

Responde **204** sin cuerpo.

- **400** si es la única foto que le queda al producto.
- **403** si el usuario no es el dueño del producto.
- **404** si el producto no existe, o si esa foto no le pertenece.

### Formato de las fotos en la respuesta

`ProductoRespuestaDTO` devuelve las fotos como objetos con `id` y `url`,
porque el `id` es lo que permite armar el `DELETE` de una foto puntual:

```json
{
  "id": 1,
  "nombre": "Remera",
  "fotos": [
    { "id": 1, "url": "https://cdn.ejemplo.com/remera-frente.jpg" },
    { "id": 2, "url": "https://cdn.ejemplo.com/remera-espalda.jpg" }
  ]
}
```

En el alta (`ProductoCrearDTO`) y en `AgregarFotosDTO` las fotos se
siguen mandando como una lista de strings, porque todavía no tienen id.
