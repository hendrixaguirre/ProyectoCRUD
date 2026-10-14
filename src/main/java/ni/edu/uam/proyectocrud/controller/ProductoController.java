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

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

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
        configurarTableView();
        configurarSeleccionTableView();
        cargarCategorias();
        cargarMarcas();
        cargarOpcionesOrden();
        cargarProductos();
        dtpFecha.setValue(LocalDate.now());
    }

    private void configurarTableView() {
        colCodigo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCodigo()));
        colNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombre()));
        colCategoria.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategoria()));
        colMarca.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMarca()));
        colPrecio.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getPrecio()).asObject());
        colStock.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getStock()).asObject());
        colFechaCreacion.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getFecha_creacion()));
        tblProductos.setItems(listaProductos);
    }

    private void configurarSeleccionTableView() {
        tblProductos.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        cargarProductoEnFormulario(newValue);
                    }
                });
    }

    /**
     * Recibe como parámetro un producto seleccionado del TableView
     * @param p producto seleccionado
     */
    private void cargarProductoEnFormulario(Producto p) {
        txtCodigo.setText(p.getCodigo());
        txtNombre.setText(p.getNombre());
        txtDescripcion.setText(p.getDescripcion());
        txtPrecio.setText(String.valueOf(p.getPrecio()));
        txtStock.setText(String.valueOf(p.getStock()));
        if (p.getFecha_creacion() != null) {
            Date fecha = new Date(p.getFecha_creacion().getTime());
            dtpFecha.setValue(fecha.toLocalDate());
        }
        for (Categoria categoria : listaCategorias) {
            if (categoria.getId_categoria() == p.getId_categoria()) {
                cmbCategoria.setValue(categoria);
                break;
            }
        }
        for (Marca marca : listaMarcas) {
            if (marca.getId_marca() == p.getId_marca()) {
                cmbMarca.setValue(marca);
                break;
            }
        }
    }


    /*Carga categorías activas desde la base de datos*/
    private void cargarCategorias() {
        listaCategorias.clear();
        String sql = """
                SELECT id_categoria, nombre
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
            cmbCategoria.setItems(FXCollections.observableArrayList(listaCategorias));

        } catch (SQLException e) {mostrarErrorBaseDatos("No se pudieron cargar las categorías.", e);
        }
    }

    /**
     * Carga marcas activas desde la base de datos
     */
    private void cargarMarcas() {
        listaMarcas.clear();
        String sql = """
                SELECT id_marca, nombre
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

        } catch (SQLException e) {mostrarErrorBaseDatos("No se pudieron cargar las marcas.", e);
        }
    }

    private void cargarOpcionesOrden() {
        cmbOrdenar.setItems(
                FXCollections.observableArrayList("Nombre", "Precio menor a mayor", "Precio mayor a menor", "Fecha más reciente", "Fecha más antigua", "Stock mayor a menor", "Stock menor a mayor"));
        cmbOrdenar.setValue("Nombre");
    }

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
                    P.id_producto,
                    P.codigo,
                    P.nombre,
                    P.descripcion,
                    P.precio,
                    P.stock,
                    P.fecha_creacion,
                    P.id_categoria,
                    C.nombre AS categoria,
                    P.id_marca,
                    M.nombre AS marca,
                    P.estado
                FROM catalogos.productos P
                INNER JOIN catalogos.categorias C
                    ON P.id_categoria = C.id_categoria
                INNER JOIN catalogos.marcas M
                    ON P.id_marca = M.id_marca
                ORDER BY P.nombre
                """;

        try (
                Connection conexion = ConexionBD.getConnection();
                PreparedStatement ps = conexion.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                listaProductos.add(crearProductoDesdeResultSet(rs)
                );
            }

        } catch (SQLException e) {
            mostrarErrorBaseDatos("No se pudieron cargar los productos.", e);
        }
    }

    @FXML
    private void guardarProducto() {
        if (!validarCampos()) {
            return;
        }
        Categoria categoriaSeleccionada = cmbCategoria.getValue();
        Marca marcaSeleccionada = cmbMarca.getValue();

        String sql = """
                INSERT INTO catalogos.productos
                (codigo, nombre, descripcion, precio, stock,
                 fecha_creacion, id_categoria, id_marca)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (
                Connection conexion = ConexionBD.getConnection();
                PreparedStatement ps = conexion.prepareStatement(sql)
        ) {
            ps.setString(1, txtCodigo.getText());
            ps.setString(2, txtNombre.getText());
            ps.setString(3, txtDescripcion.getText());
            ps.setDouble(4, Double.parseDouble(txtPrecio.getText()));
            ps.setInt(5, Integer.parseInt(txtStock.getText()));
            ps.setDate(6, Date.valueOf(dtpFecha.getValue()));
            ps.setInt(7, categoriaSeleccionada.getId_categoria());
            ps.setInt(8, marcaSeleccionada.getId_marca());
            ps.executeUpdate();
            mostrarAlerta(Alert.AlertType.INFORMATION, "Registro almacenado", "Producto registrado", "El producto se ha almacenado exitosamente"
            );
            crearProducto();
            cargarProductos();
        } catch (SQLException e) {
            mostrarErrorBaseDatos("No se pudo guardar el producto.", e);
        }
    }

    @FXML
    private void actualizarProducto() {
        Producto productoSeleccionado = tblProductos.getSelectionModel().getSelectedItem();
        if (productoSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección requerida", "No hay producto seleccionado", "Seleccione un producto de la lista");
            return;
        }
        if (!validarCampos()) {
            return;
        }
        Categoria categoriaSeleccionada = cmbCategoria.getValue();
        Marca marcaSeleccionada = cmbMarca.getValue();

        String sql = """
                UPDATE catalogos.productos
                SET codigo = ?,
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
            ps.setString(1, txtCodigo.getText());
            ps.setString(2, txtNombre.getText());
            ps.setString(3, txtDescripcion.getText());
            ps.setDouble(4, Double.parseDouble(txtPrecio.getText()));
            ps.setInt(5, Integer.parseInt(txtStock.getText()));
            ps.setDate(6, Date.valueOf(dtpFecha.getValue()));
            ps.setInt(7, categoriaSeleccionada.getId_categoria());
            ps.setInt(8, marcaSeleccionada.getId_marca());
            ps.setInt(9, productoSeleccionado.getId_producto());
            int filasActualizadas = ps.executeUpdate();

            if (filasActualizadas > 0) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Registro actualizado", "Actualización completada", "El producto fue actualizado correctamente");
                crearProducto();
                cargarProductos();
            }

        } catch (SQLException e) {
            mostrarErrorBaseDatos("No se pudo actualizar el producto.", e);
        }
    }

    @FXML
    private void eliminarProducto() {
        Producto productoSeleccionado = tblProductos.getSelectionModel().getSelectedItem();
        if (productoSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección requerida", "No hay producto seleccionado", "Seleccione un producto de la lista");
            return;
        }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmación");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Está seguro que desea eliminar el registro del producto?");

        if (confirmacion.showAndWait().isEmpty() || confirmacion.getResult() != ButtonType.OK) {
            return;
        }
        String sql = "DELETE FROM catalogos.productos WHERE id_producto = ?";

        try (
                Connection conexion = ConexionBD.getConnection();
                PreparedStatement ps = conexion.prepareStatement(sql)
        ) {
            ps.setInt(1, productoSeleccionado.getId_producto());
            int filasEliminadas = ps.executeUpdate();

            if (filasEliminadas > 0) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Registro eliminado", "Eliminación completada", "El producto fue eliminado exitosamente");
                crearProducto();
                cargarProductos();
            }

        } catch (SQLException e) {
            mostrarErrorBaseDatos("No se pudo eliminar el producto.", e);
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

    private boolean validarCampos() {
        if (txtCodigo.getText().trim().isEmpty() || txtNombre.getText().trim().isEmpty() || txtPrecio.getText().trim().isEmpty() || txtStock.getText().trim().isEmpty() || cmbCategoria.getValue() == null || cmbMarca.getValue() == null || dtpFecha.getValue() == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos requeridos", "Datos incompletos", "Complete todos los campos obligatorios");
            return false;
        }
        try {
            double precio = Double.parseDouble(txtPrecio.getText());

            if (precio < 0) {
                mostrarAlerta(Alert.AlertType.WARNING, "Precio incorrecto", "Precio no válido", "El precio no puede ser negativo");
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Precio incorrecto", "Precio no válido", "Ingrese un número válido en el precio");
            return false;
        }
        try {
            int stock = Integer.parseInt(txtStock.getText());

            if (stock < 0) {
                mostrarAlerta(Alert.AlertType.WARNING, "Stock incorrecto", "Stock no válido", "El stock no puede ser negativo");
                return false;
            }

        } catch (NumberFormatException e) {
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

    @FXML
    private void limpiarFiltro(){

    }

    @FXML
    private void buscarRegistro(){

    }
}