package ni.edu.uam.proyectocrud.controller;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ni.edu.uam.proyectocrud.database.ConexionBD;
import ni.edu.uam.proyectocrud.model.Categoria;
import ni.edu.uam.proyectocrud.model.Marca;
import ni.edu.uam.proyectocrud.model.Producto;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductoController {

    @FXML
    private TextField txtCodigo;
    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtPrecio;
    @FXML
    private TextField txtStock;
    @FXML
    private ComboBox<Categoria> cmbCategoria;
    @FXML
    private ComboBox<Marca> cmbMarca;
    @FXML
    private DatePicker dtpFecha;
    @FXML
    private TextField txtDescripcion;
    @FXML
    private TextField txtCodigoNombre;
    @FXML
    private ComboBox<Categoria> cmbCategoriaBusqueda;
    @FXML
    private DatePicker dtpDesde;
    @FXML
    private DatePicker dtpHasta;
    @FXML
    private TextField txtPrecioMinimo;
    @FXML
    private TextField txtPrecioMaximo;
    @FXML
    private CheckBox chkProductosExistencia;
    @FXML
    private ComboBox<String> cmbOrdenar;
    @FXML
    private TableView<Producto> tblProductos;
    @FXML
    private TableColumn<Producto, String> colCodigo;
    @FXML
    private TableColumn<Producto, String> colNombre;
    @FXML
    private TableColumn<Producto, String> colCategoria;
    @FXML
    private TableColumn<Producto, String> colMarca;
    @FXML
    private TableColumn<Producto, Double> colPrecio;
    @FXML
    private TableColumn<Producto, Integer> colStock;
    @FXML
    private TableColumn<Producto, java.util.Date> colFechaCreacion;

    private final ObservableList<Producto> listaProductos = FXCollections.observableArrayList();
    private final ObservableList<Categoria> listaCategorias = FXCollections.observableArrayList();
    private final ObservableList<Marca> listaMarcas = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        configurarTabla();
        configurarSeleccionTableView();
        cargarCategorias();
        cargarMarcas();
        configurarOrdenamiento();
        cargarProductos();
        dtpFecha.setValue(LocalDate.now());
    }

    private void configurarTabla() {
        colCodigo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCodigo()));
        colNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombre()));
        colCategoria.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategoria()));
        colMarca.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMarca()));
        colPrecio.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getPrecio()).asObject());
        colStock.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getStock()).asObject());
        colFechaCreacion.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getFecha_creacion()));
        tblProductos.setItems(listaProductos);
    }

    // Selección
    private void configurarSeleccionTableView() {
        tblProductos.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                cargarProductoSeleccionado(newValue);
            }
        });
    }

    /**
     * Recibe como parámetro un producto seleccionado del TableView
     * @param p, es el producto seleccionado del TableView
     */
    private void cargarProductoSeleccionado(Producto p) {
        txtCodigo.setText(p.getCodigo());
        txtNombre.setText(p.getNombre());
        txtDescripcion.setText(p.getDescripcion());
        txtPrecio.setText(String.valueOf(p.getPrecio()));
        txtStock.setText(String.valueOf(p.getStock()));
        if (p.getFecha_creacion() != null) {
            Date fecha = new Date(p.getFecha_creacion().getTime());
            dtpFecha.setValue(fecha.toLocalDate());
        }

        /**
         * Buscamos dentro del ComboBox el valor que corresponda al ID
         */
        for (Categoria categoria : listaCategorias) {
            if (categoria.getId_categoria() == p.getId_categoria()) {
                cmbCategoria.setValue(categoria);
                break;
            }
        }

        /**
         * Buscamos dentro del ComboBox el valor que corresponda al ID
         */
        for (Marca marca : listaMarcas) {
            if (marca.getId_marca() == p.getId_marca()) {
                cmbMarca.setValue(marca);
                break;
            }
        }
    }

    /**
     * Consulta las categorias activas de la base de datos para mostrarlas dentro del ComboBox
     */
    private void cargarCategorias() {
        listaCategorias.clear();
        String sql = """
                SELECT
                    id_categoria,
                    nombre
                FROM catalogos.categorias
                WHERE estado = 1
                ORDER BY nombre
                """;
        try (
                Connection conexion = ConexionBD.getConnection();
                PreparedStatement ps = conexion.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                Categoria categoria = new Categoria();
                categoria.setId_categoria(rs.getInt("id_categoria"));
                categoria.setNombre(rs.getString("nombre"));
                listaCategorias.add(categoria);
            }

            /*
             * Utilizamos copias de la lista para evitar que
             * cada ComboBox tenga problemas de selección.
             */
            cmbCategoria.setItems(FXCollections.observableArrayList(listaCategorias));
            cmbCategoriaBusqueda.setItems(FXCollections.observableArrayList(listaCategorias));

        } catch (SQLException e) {
            mostrarErrorBaseDatos("No se pudieron cargar las categorías.", e);
        }
    }

    /**
     * Consulta las marcas activas almacenadas en PostgreSQL.
     *
     * Estas marcas se agregan al ComboBox utilizado en
     * el formulario de productos.
     */
    private void cargarMarcas() {
        listaMarcas.clear();
        String sql = """
                SELECT
                    id_marca,
                    nombre
                FROM catalogos.marcas
                WHERE estado = 1
                ORDER BY nombre
                """;
        try (
                Connection conexion = ConexionBD.getConnection();
                PreparedStatement ps = conexion.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                Marca marca = new Marca();
                marca.setId_marca(rs.getInt("id_marca"));
                marca.setNombre(rs.getString("nombre"));
                listaMarcas.add(marca);
            }
            cmbMarca.setItems(FXCollections.observableArrayList(listaMarcas));

        } catch (SQLException e) {
            mostrarErrorBaseDatos("No se pudieron cargar las marcas.", e);
        }
    }

    /**
     * Agrega al ComboBox las opciones disponibles para
     * ordenar los resultados.
     *
     * El usuario no introduce directamente el nombre
     * de una columna SQL.
     *
     * Esto permite utilizar una whitelist de valores
     * conocidos y evita concatenar texto arbitrario
     * dentro de ORDER BY.
     */
    private void configurarOrdenamiento() {
        cmbOrdenar.setItems(FXCollections.observableArrayList("Nombre", "Precio menor a mayor", "Precio mayor a menor", "Fecha más reciente", "Fecha más antigua", "Stock mayor a menor", "Stock menor a mayor"));
        cmbOrdenar.setValue("Nombre");
    }

    // Crear producto desde resultset permite escribir los objetos como filas
    private Producto crearProductoDesdeResultSet(ResultSet rs) throws SQLException {
        Producto producto = new Producto();
        producto.setId_producto(rs.getInt("id_producto"));
        producto.setCodigo(rs.getString("codigo"));
        producto.setNombre(rs.getString("nombre"));
        producto.setDescripcion(rs.getString("descripcion"));
        producto.setPrecio(rs.getDouble("precio"));
        producto.setStock(rs.getInt("stock"));
        producto.setFecha_creacion(rs.getDate("fecha_creacion"));
        producto.setId_categoria(rs.getInt("id_categoria"));
        producto.setCategoria(rs.getString("categoria"));
        producto.setId_marca(rs.getInt("id_marca"));
        producto.setMarca(rs.getString("marca"));
        producto.setEstado(rs.getInt("estado"));
        return producto;
    }

    @FXML
    private void cargarProductos() {
        listaProductos.clear();
        String sql = """
                SELECT
                    p.id_producto,
                    p.codigo,
                    p.nombre,
                    p.descripcion,
                    p.precio,
                    p.stock,
                    p.fecha_creacion,
                    c.id_categoria,
                    c.nombre AS categoria,
                    m.id_marca,
                    m.nombre AS marca,
                    p.estado
                FROM catalogos.productos p
                INNER JOIN catalogos.categorias c
                    ON p.id_categoria = c.id_categoria
                INNER JOIN catalogos.marcas m
                    ON p.id_marca = m.id_marca
                WHERE p.estado = 1
                ORDER BY p.nombre
                """;
        try (
                Connection conexion = ConexionBD.getConnection();
                PreparedStatement ps = conexion.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                listaProductos.add(crearProductoDesdeResultSet(rs));
            }
            tblProductos.setItems(listaProductos);
        } catch (SQLException e) {
            mostrarErrorBaseDatos("No se pudieron cargar los productos.", e);
        }
    }

    @FXML
    private void guardarProducto() {
        if (!validarCampos()) {
            return;
        }
        String codigo = txtCodigo.getText().trim();

        /*
         * Validación preventiva de UNIQUE.
         */
        if (existeCodigo(codigo, null)) {
            mostrarAlerta(Alert.AlertType.WARNING, "Código duplicado", "Código duplicado", "El código " + codigo + " ya pertenece a otro producto.");
            txtCodigo.requestFocus();
            return;
        }

        String sql = """
                INSERT INTO catalogos.productos
                (
                    codigo,
                    nombre,
                    descripcion,
                    precio,
                    stock,
                    fecha_creacion,
                    id_categoria,
                    id_marca,
                    estado
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1)
                """;
        try (
                Connection conexion = ConexionBD.getConnection();
                PreparedStatement ps = conexion.prepareStatement(sql)
        ) {
            ps.setString(1, codigo);
            ps.setString(2, txtNombre.getText().trim());
            ps.setString(3, txtDescripcion.getText().trim());
            ps.setBigDecimal(4, new BigDecimal(txtPrecio.getText().trim()));
            ps.setInt(5, Integer.parseInt(txtStock.getText().trim()));
            ps.setDate(6, Date.valueOf(dtpFecha.getValue()));
            ps.setInt(7, cmbCategoria.getValue().getId_categoria());
            ps.setInt(8, cmbMarca.getValue().getId_marca());

            int filasAfectadas = ps.executeUpdate();

            if (filasAfectadas > 0) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Registro guardado", "Producto registrado", "El producto fue registrado correctamente.");
                crearProducto();
                cargarProductos();
            }
        } catch (SQLException e) {
            /*
             * Aunque JavaFX realizó previamente la comprobación,
             * PostgreSQL continúa siendo la autoridad final.
             *
             * SQLState 23505 corresponde normalmente a una
             * violación de UNIQUE en PostgreSQL.
             */
            if ("23505".equals(e.getSQLState())) {
                mostrarAlerta(Alert.AlertType.WARNING, "Registro duplicado", "Código duplicado", "El código ingresado ya existe.");
            } else {
                mostrarErrorBaseDatos("No se pudo guardar el producto.", e);
            }
        }
    }

    @FXML
    private void actualizarProducto() {
        Producto productoSeleccionado = tblProductos.getSelectionModel().getSelectedItem();

        if (productoSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Producto no seleccionado", "Producto no seleccionado", "Seleccione un producto del TableView antes de actualizar.");
            return;
        }
        if (!validarCampos()) {
            return;
        }
        String codigo = txtCodigo.getText().trim();
        /*
         * Se comprueba que otro producto diferente
         * no esté utilizando el mismo código.
         */
        if (existeCodigo(codigo, productoSeleccionado.getId_producto())) {
            mostrarAlerta(Alert.AlertType.WARNING, "Código duplicado", "Código duplicado", "El código ingresado pertenece a otro producto.");
            txtCodigo.requestFocus();
            return;
        }
        String sql = """
                UPDATE catalogos.productos
                SET
                    codigo = ?,
                    nombre = ?,
                    descripcion = ?,
                    precio = ?,
                    stock = ?,
                    fecha_creacion = ?,
                    id_categoria = ?,
                    id_marca = ?
                WHERE id_producto = ?
                """;
        try (
                Connection conexion = ConexionBD.getConnection();
                PreparedStatement ps = conexion.prepareStatement(sql)
        ) {
            ps.setString(1, codigo);
            ps.setString(2, txtNombre.getText().trim());
            ps.setString(3, txtDescripcion.getText().trim());
            ps.setBigDecimal(4, new BigDecimal(txtPrecio.getText().trim()));
            ps.setInt(5, Integer.parseInt(txtStock.getText().trim()));
            ps.setDate(6, Date.valueOf(dtpFecha.getValue()));
            ps.setInt(7, cmbCategoria.getValue().getId_categoria());
            ps.setInt(8, cmbMarca.getValue().getId_marca());
            ps.setInt(9, productoSeleccionado.getId_producto());

            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas > 0) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Registro actualizado", "Actualización completada", "El producto fue actualizado correctamente.");
                crearProducto();
                cargarProductos();
            }
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                mostrarAlerta(Alert.AlertType.WARNING, "Código duplicado", "Código duplicado", "El código ingresado ya está siendo utilizado.");
            } else {
                mostrarErrorBaseDatos("No se pudo actualizar el producto.", e);
            }
        }
    }

    @FXML
    private void eliminarProducto() {
        Producto productoSeleccionado = tblProductos.getSelectionModel().getSelectedItem();
        if (productoSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Producto no seleccionado", "Producto no seleccionado", "Seleccione un producto antes de eliminarlo.");
            return;
        }

        /*
         * Se solicita confirmación para evitar que el usuario
         * desactive accidentalmente un registro.
         */
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);

        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("Eliminar producto");
        confirmacion.setContentText("¿Desea desactivar el producto " + productoSeleccionado.getNombre() + "?");
        Optional<ButtonType> respuesta = confirmacion.showAndWait();

        if (respuesta.isEmpty() || respuesta.get() != ButtonType.OK) {
            return;
        }
        String sql = """
                UPDATE catalogos.productos
                SET estado = 0
                WHERE id_producto = ?
                """;
        try (
                Connection conexion = ConexionBD.getConnection();
                PreparedStatement ps = conexion.prepareStatement(sql)
        ) {
            ps.setInt(1, productoSeleccionado.getId_producto());
            int filasAfectadas = ps.executeUpdate();

            if (filasAfectadas > 0) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Producto eliminado", "Eliminación completada", "El producto fue desactivado correctamente.");
                crearProducto();
                cargarProductos();
            }

        } catch (SQLException e) {
            mostrarErrorBaseDatos("No se pudo eliminar el producto.", e);
        }
    }

    private boolean existeCodigo(String codigo, Integer idProductoExcluir) {
        String sql;
        if (idProductoExcluir == null) {
            sql = """
                    SELECT id_producto
                    FROM catalogos.productos
                    WHERE codigo = ?
                    """;
        } else {
            sql = """
                    SELECT id_producto
                    FROM catalogos.productos
                    WHERE codigo = ?
                    AND id_producto <> ?
                    """;
        }
        try (
                Connection conexion = ConexionBD.getConnection();
                PreparedStatement ps = conexion.prepareStatement(sql)
        ) {
            ps.setString(1, codigo);

            if (idProductoExcluir != null) {
                ps.setInt(2, idProductoExcluir);
            }

            try (
                    ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            mostrarErrorBaseDatos("No se pudo verificar el código del producto.", e);
            return false;
        }
    }

    @FXML
    private void crearProducto() {
        txtCodigo.clear();
        txtNombre.clear();
        txtPrecio.clear();
        txtStock.clear();
        txtDescripcion.clear();
        cmbCategoria.getSelectionModel().clearSelection();
        cmbMarca.getSelectionModel().clearSelection();
        dtpFecha.setValue(LocalDate.now());
        tblProductos.getSelectionModel().clearSelection();
        txtCodigo.requestFocus();
    }

    @FXML
    private void buscarProducto() {
        listaProductos.clear();
        /*
         * La consulta inicia con los INNER JOIN necesarios
         * y con una condición permanente:
         *
         * p.estado = 1
         *
         * De esta forma nunca mostramos productos
         * eliminados lógicamente.
         */
        StringBuilder sql = new StringBuilder("""
                SELECT
                    p.id_producto,
                    p.codigo,
                    p.nombre,
                    p.descripcion,
                    p.precio,
                    p.stock,
                    p.fecha_creacion,
                    c.id_categoria,
                    c.nombre AS categoria,
                    m.id_marca,
                    m.nombre AS marca,
                    p.estado
                FROM catalogos.productos p
                INNER JOIN catalogos.categorias c
                    ON p.id_categoria = c.id_categoria
                INNER JOIN catalogos.marcas m
                    ON p.id_marca = m.id_marca
                WHERE p.estado = 1
                """);

        /*
         * Aquí almacenaremos los valores que posteriormente
         * serán enviados al PreparedStatement.
         */
        List<Object> parametros = new ArrayList<>();
        String texto = txtCodigoNombre.getText().trim();
        if (!texto.isBlank()) {

            /*
             * OR permite que el texto coincida con código
             * o nombre.
             *
             * ILIKE permite búsquedas parciales sin importar
             * mayúsculas y minúsculas.
             */
            sql.append("""
                    
                    AND (
                        p.codigo ILIKE ?
                        OR p.nombre ILIKE ?
                    )
                    """);

            parametros.add("%" + texto + "%");
            parametros.add("%" + texto + "%");
        }

        Categoria categoria = cmbCategoriaBusqueda.getValue();

        if (categoria != null) {
            sql.append("""
                    
                    AND p.id_categoria = ?
                    """);

            parametros.add(categoria.getId_categoria());
        }

        LocalDate fechaDesde = dtpDesde.getValue();

        if (fechaDesde != null) {
            sql.append("""
                    
                    AND p.fecha_creacion >= ?
                    """);

            parametros.add(fechaDesde);
        }

        LocalDate fechaHasta = dtpHasta.getValue();

        if (fechaHasta != null) {
            sql.append("""
                    
                    AND p.fecha_creacion <= ?
                    """);

            parametros.add(fechaHasta);
        }

        /*
         * Verificamos que las fechas tengan sentido.
         *
         * "Desde" no debería ser posterior a "Hasta".
         */
        if (fechaDesde != null && fechaHasta != null && fechaDesde.isAfter(fechaHasta)) {

            mostrarAlerta(Alert.AlertType.WARNING, "Rango de fechas incorrecto", "Fechas incorrectas", "La fecha inicial no puede ser posterior a la fecha final.");
            return;
        }
        BigDecimal precioMinimo = null;

        if (!txtPrecioMinimo.getText().isBlank()) {
            try {
                precioMinimo = new BigDecimal(txtPrecioMinimo.getText().trim());
                if (precioMinimo.compareTo(
                        BigDecimal.ZERO) < 0) {
                    mostrarAlerta(Alert.AlertType.WARNING, "Precio incorrecto", "Precio incorrecto", "El precio mínimo no puede ser negativo.");
                    return;
                }
                sql.append("""
                        
                        AND p.precio >= ?
                        """);
                parametros.add(precioMinimo);

            } catch (NumberFormatException e) {

                mostrarAlerta(Alert.AlertType.WARNING, "Precio incorrecto", "Precio incorrecto", "El precio mínimo debe ser numérico.");
                txtPrecioMinimo.requestFocus();
                return;
            }
        }

        BigDecimal precioMaximo = null;
        if (!txtPrecioMaximo.getText().isBlank()) {
            try {
                precioMaximo = new BigDecimal(txtPrecioMaximo.getText().trim());

                if (precioMaximo.compareTo(
                        BigDecimal.ZERO) < 0) {
                    mostrarAlerta(Alert.AlertType.WARNING, "Precio incorrecto", "Precio incorrecto", "El precio máximo no puede ser negativo.");
                    return;
                }

                sql.append("""
                        
                        AND p.precio <= ?
                        """);
                parametros.add(precioMaximo);

            } catch (NumberFormatException e) {
                mostrarAlerta(Alert.AlertType.WARNING, "Precio incorrecto", "Precio incorrecto", "El precio máximo debe ser numérico.");
                txtPrecioMaximo.requestFocus();
                return;
            }
        }
        /*
         * Validamos que el precio mínimo no sea superior
         * al precio máximo.
         */
        if (precioMinimo != null && precioMaximo != null && precioMinimo.compareTo(precioMaximo) > 0) {
            mostrarAlerta(Alert.AlertType.WARNING, "Rango de precios incorrecto", "Precios incorrectos", "El precio mínimo no puede ser mayor que el precio máximo.");
            return;
        }

        if (chkProductosExistencia.isSelected()) {
            sql.append("""
                    
                    AND p.stock > 0
                    """);
        }

        String orden = cmbOrdenar.getValue();

        if (orden == null) {
            orden = "Nombre";
        }
        switch (orden) {
            case "Precio menor a mayor" ->
                    sql.append("""
                            
                            ORDER BY p.precio ASC
                            """);
            case "Precio mayor a menor" ->
                    sql.append("""
                            
                            ORDER BY p.precio DESC
                            """);
            case "Fecha más reciente" ->
                    sql.append("""
                            
                            ORDER BY p.fecha_creacion DESC
                            """);
            case "Fecha más antigua" ->
                    sql.append("""
                            
                            ORDER BY p.fecha_creacion ASC
                            """);
            case "Stock mayor a menor" ->
                    sql.append("""
                            
                            ORDER BY p.stock DESC
                            """);
            case "Stock menor a mayor" ->
                    sql.append("""
                            
                            ORDER BY p.stock ASC
                            """);
            default ->
                    sql.append("""
                            
                            ORDER BY p.nombre ASC
                            """);
        }

        try (
                Connection conexion = ConexionBD.getConnection();
                PreparedStatement ps = conexion.prepareStatement(sql.toString())) {
            /*
             * Recorremos la lista de parámetros para
             * asignarlos dinámicamente al PreparedStatement.
             */
            for (int i = 0; i < parametros.size(); i++) {
                Object valor = parametros.get(i);

                if (valor instanceof LocalDate fecha) {
                    ps.setDate(i + 1, Date.valueOf(fecha));

                } else if (valor instanceof BigDecimal numero) {
                    ps.setBigDecimal(i + 1, numero);

                } else {
                    ps.setObject(i + 1, valor);
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    listaProductos.add(crearProductoDesdeResultSet(rs));
                }
            }
            tblProductos.setItems(listaProductos);

            if (listaProductos.isEmpty()) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Sin resultados", "Búsqueda completada", "No existen productos que coincidan con los filtros aplicados.");
            }

        } catch (SQLException e) {
            mostrarErrorBaseDatos("No se pudo realizar la búsqueda.", e);
        }
    }

    @FXML
    private void limpiarFiltro() {
        txtCodigoNombre.clear();
        cmbCategoriaBusqueda.getSelectionModel().clearSelection();
        dtpDesde.setValue(null);
        dtpHasta.setValue(null);
        txtPrecioMinimo.clear();
        txtPrecioMaximo.clear();
        chkProductosExistencia.setSelected(false);
        cmbOrdenar.setValue("Nombre");
        cargarProductos();
    }

    private boolean validarCampos() {
        if (txtCodigo.getText().trim().isEmpty() || txtNombre.getText().trim().isEmpty() || txtPrecio.getText().trim().isEmpty() || txtStock.getText().trim().isEmpty() || cmbCategoria.getValue() == null || cmbMarca.getValue() == null || dtpFecha.getValue() == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos requeridos", "Datos incompletos", "Complete todos los campos obligatorios");
            return false;
        }

        try {
            BigDecimal precio = new BigDecimal(txtPrecio.getText().trim());

            if (precio.compareTo(BigDecimal.ZERO) <= 0) {
                mostrarAlerta(Alert.AlertType.WARNING, "Precio incorrecto", "Precio no válido", "El precio debe ser mayor que cero");
                return false;
            }

        } catch (NumberFormatException ex) {
            mostrarAlerta(Alert.AlertType.WARNING, "Precio incorrecto", "Precio no válido", "Ingrese un número válido en el precio");
            return false;
        }

        try {
            int stock = Integer.parseInt(txtStock.getText().trim());

            if (stock < 0) {
                mostrarAlerta(Alert.AlertType.WARNING, "Stock incorrecto", "Stock no válido", "El stock no puede ser negativo");
                return false;
            }

        } catch (NumberFormatException ex) {
            mostrarAlerta(Alert.AlertType.WARNING, "Stock incorrecto", "Stock no válido", "Ingrese un número entero en el stock");
            return false;
        }
        return true;
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String encabezado, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(encabezado);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarErrorBaseDatos(String mensaje, SQLException e) {
        mostrarAlerta(Alert.AlertType.ERROR, "Error de base de datos", "Ocurrió un error", mensaje + "\n" + e.getMessage());
        e.printStackTrace();
    }
}