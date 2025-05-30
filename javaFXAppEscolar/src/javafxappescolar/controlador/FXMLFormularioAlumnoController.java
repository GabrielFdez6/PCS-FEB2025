package javafxappescolar.controlador;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafxappescolar.dominio.AlumnoDM;
import javafxappescolar.interfaz.INotificacion;
import javafxappescolar.modelo.dao.AlumnoDAO;
import javafxappescolar.modelo.dao.CatalogoDAO;
import javafxappescolar.modelo.pojo.Alumno;
import javafxappescolar.modelo.pojo.Carrera;
import javafxappescolar.modelo.pojo.Facultad;
import javafxappescolar.modelo.pojo.ResultadoOperacion;
import javafxappescolar.utilidades.Utilidad;
import javax.imageio.ImageIO;

/**
 * FXML Controller class
 *
 * @author gabriel-fdez
 */
public class FXMLFormularioAlumnoController implements Initializable {

    @FXML
    private ImageView ivFoto;
    @FXML
    private TextField tfNombre;
    @FXML
    private TextField tfApellidoPaterno;
    @FXML
    private TextField tfApellidoMaterno;
    @FXML
    private TextField tfMatricula;
    @FXML
    private TextField tfEmail;
    @FXML
    private DatePicker dpFechaNacimiento;
    @FXML
    private ComboBox<Facultad> cbFacultad;
    @FXML
    private ComboBox<Carrera> cbCarrera;

    ObservableList<Facultad> facultades;
    ObservableList<Carrera> carreras;
    File archivoFoto;
    INotificacion observador;
    Alumno alumnoEdicion;
    boolean esEdicion;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cargarFacultades();
        seleccionarFacultad();
    }

    public void inicializarInformacion(boolean esEdicion, Alumno alumnoEdicion, INotificacion observador) {
        this.esEdicion = esEdicion;
        this.alumnoEdicion = alumnoEdicion;
        this.observador = observador;

        if (esEdicion) {
            cargarInformacionEdicion();
        }
    }

    private void cargarInformacionEdicion(){
        tfNombre.setText(alumnoEdicion.getNombre());
        tfApellidoMaterno.setText(alumnoEdicion.getApellidoMaterno());
        tfApellidoPaterno.setText(alumnoEdicion.getApellidoPaterno());
        tfMatricula.setText(alumnoEdicion.getMatricula());
        tfEmail.setText(alumnoEdicion.getEmail());
        if(alumnoEdicion.getFechaNacimiento() != null)
            dpFechaNacimiento.setValue(LocalDate.parse(alumnoEdicion.getFechaNacimiento()));
        tfMatricula.setDisable(true);

        int indice = obtenerPosicionFacultad(alumnoEdicion.getIdFacultad());
        cbFacultad.getSelectionModel().select(indice);

        int indiceCarrera = obtenerPosicionCarrera(alumnoEdicion.getIdCarrera());
        cbCarrera.getSelectionModel().select(indiceCarrera);

        try {
            byte[] foto = AlumnoDAO.obtenerFotoAlumno(alumnoEdicion.getIdAlumno());
            alumnoEdicion.setFoto(foto);
            ByteArrayInputStream input = new ByteArrayInputStream(foto);
            Image image = new Image(input);
            ivFoto.setImage(image);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void cargarFacultades(){
        try {
            facultades = FXCollections.observableArrayList();
            List<Facultad> facultadesDAO = CatalogoDAO.obtenerFacultades();
            facultades.addAll(facultadesDAO);
            cbFacultad.setItems(facultades);
        } catch (SQLException e) {
            Utilidad.mostrarAlertaSimple(Alert.AlertType.ERROR, "Error al cargar", "Lo sentimos por el momento no se puede mostrar" +
                    "la información, por favor inténtelo más tarde.");
        }
    }

    private void seleccionarFacultad(){
        cbFacultad.valueProperty().addListener(new ChangeListener<Facultad>() {
            @Override
            public void changed(ObservableValue<? extends Facultad> observableValue, Facultad oldFacultad, Facultad newFacultad) {
                if (newFacultad != null) {
                    cargarCarreras(newFacultad.getIdFacultad());
                }
            }
        });
    }

    private void cargarCarreras(int idFacultad){
        try {
            carreras = FXCollections.observableArrayList();
            List<Carrera> carreraDAO = CatalogoDAO.obtenerCarrerasPorFacultad(idFacultad);
            carreras.addAll(carreraDAO);
            cbCarrera.setItems(carreras);
        } catch (SQLException e) {
            Utilidad.mostrarAlertaSimple(Alert.AlertType.ERROR, "Error al cargar", "Lo sentimos por el momento no se puede mostrar" +
                    "la información, por favor inténtelo más tarde.");
        }
    }

    @FXML
    private void clicSeleccionarFoto(ActionEvent event) {
        mostrarDialogoSeleccionFoto();
    }

    @FXML
    private void clicGuardar(ActionEvent event) {
        if (validarCampos()) {
            try {
                if(!esEdicion) {
                    ResultadoOperacion resultado = AlumnoDM.verificarEstadoMatricula(tfMatricula.getText());
                    if (!resultado.isError()) {
                        Alumno alumno = obtenerAlumnoNuevo();
                        guardarAlumno(alumno);
                    } else {
                        Utilidad.mostrarAlertaSimple(Alert.AlertType.ERROR, "Verificar datos", resultado.getMensaje());
                    }
                } else {
                    Alumno alumno = obtenerAlumnoEdicion();
                    modificarAlumno(alumno);
                }
            } catch (IOException ex) {
                Utilidad.mostrarAlertaSimple(Alert.AlertType.ERROR, "Error en foto", "Lo sentimos la foto seleccionada no puede ser guardada");
            }
        }
    }

    @FXML
    private void clicCancelar(ActionEvent event) {
        Utilidad.getEscenarioComponente(tfNombre).close();
    }

    private void mostrarDialogoSeleccionFoto() {
        FileChooser dialogoSeleccion = new FileChooser();
        dialogoSeleccion.setTitle("Seleccion una foto");
        FileChooser.ExtensionFilter filtroImg = new FileChooser.ExtensionFilter("Archivos JPG (.jpg)", "*.jpg");
        dialogoSeleccion.getExtensionFilters().add(filtroImg);
        archivoFoto = dialogoSeleccion.showOpenDialog(Utilidad.getEscenarioComponente(tfNombre));

        if (archivoFoto != null) {
            mostrarFotoPerfil(archivoFoto);
        }
    }

    private void mostrarFotoPerfil(File archivoFoto){
        try {
            BufferedImage bufferImg = ImageIO.read(archivoFoto);
            Image imagen = SwingFXUtils.toFXImage(bufferImg, null);
            ivFoto.setImage(imagen);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private boolean validarCampos() {
        boolean camposValidos = true;
                //TODO
        return camposValidos;
    }

    private Alumno obtenerAlumnoNuevo() throws IOException {
        Alumno alumno = new Alumno();
        alumno.setNombre(tfNombre.getText());
        alumno.setApellidoPaterno(tfApellidoPaterno.getText());
        alumno.setApellidoMaterno(tfApellidoMaterno.getText());
        alumno.setEmail(tfEmail.getText());
        alumno.setMatricula(tfMatricula.getText());
        alumno.setFechaNacimiento(dpFechaNacimiento.getValue().toString());
        Carrera carrera = cbCarrera.getSelectionModel().getSelectedItem();
        if (carrera == null) {
            Utilidad.mostrarAlertaSimple(Alert.AlertType.WARNING, "No se seleccionó una carrera", "Por favor seleccione una carrera para el perfil del alumno");
        }
        alumno.setIdCarrera(carrera.getIdCarrera());
        if (archivoFoto == null) {
            Utilidad.mostrarAlertaSimple(Alert.AlertType.WARNING, "No se seleccionó una foto", "Por favor seleccione una foto para el perfil del alumno");
        }
        byte[] foto = Files.readAllBytes(archivoFoto.toPath());
        alumno.setFoto(foto);
        return alumno;
    }

    private Alumno obtenerAlumnoEdicion() throws IOException {
        Alumno alumno = new Alumno();
        alumno.setIdAlumno(alumnoEdicion.getIdAlumno());
        alumno.setNombre(tfNombre.getText());
        alumno.setApellidoPaterno(tfApellidoPaterno.getText());
        alumno.setApellidoMaterno(tfApellidoMaterno.getText());
        alumno.setEmail(tfEmail.getText());
        alumno.setMatricula(tfMatricula.getText());
        alumno.setFechaNacimiento(dpFechaNacimiento.getValue().toString());
        Carrera carrera = cbCarrera.getSelectionModel().getSelectedItem();
        alumno.setIdCarrera(carrera.getIdCarrera());
        if(archivoFoto != null) {
                byte[] foto = Files.readAllBytes(archivoFoto.toPath());
                alumno.setFoto(foto);
        } else {
            alumno.setFoto(alumnoEdicion.getFoto());
        }
        return alumno;
    }

    private void guardarAlumno(Alumno alumno){
        try {
            ResultadoOperacion resultadoInsertar = AlumnoDAO.registrarAlumno(alumno);
            if (!resultadoInsertar.isError()) {
                Utilidad.mostrarAlertaSimple(Alert.AlertType.INFORMATION, "Alumno(a) registrado", "El alumno(a) " + alumno.getNombre() + " fue registrado" +
                        "con éxito.");
                Utilidad.getEscenarioComponente(tfNombre).close();
                observador.operacionExitosa("insertar", alumno.getNombre());
            } else {
                Utilidad.mostrarAlertaSimple(Alert.AlertType.ERROR, "Error al registrar", resultadoInsertar.getMensaje());
            }
        } catch (SQLException ex) {
            Utilidad.mostrarAlertaSimple(Alert.AlertType.ERROR, "Problemas de conexión", "Por el momento no hay conexión");
        }
    }

    private void modificarAlumno(Alumno alumno){
        try {
            ResultadoOperacion resultadoModificar = AlumnoDAO.editarAlumno(alumno);
            if (!resultadoModificar.isError()) {
                Utilidad.mostrarAlertaSimple(Alert.AlertType.INFORMATION, "Alumno(a) modificado", "El alumno(a) " + alumno.getNombre() + " fue modificado " +
                        "con éxito.");
                Utilidad.getEscenarioComponente(tfNombre).close();
                observador.operacionExitosa("modificar", alumno.getNombre());
            } else {
                Utilidad.mostrarAlertaSimple(Alert.AlertType.ERROR, "Error al modificar", resultadoModificar.getMensaje());
            }
        } catch (SQLException ex) {
            Utilidad.mostrarAlertaSimple(Alert.AlertType.ERROR, "Problemas de conexión", "Por el momento no hay conexión");
        }
    }

    private int obtenerPosicionFacultad(int idFacultad) {
        for (int i = 0; i < facultades.size(); i++) {
            if (facultades.get(i).getIdFacultad() == idFacultad)
                return i;
        }
        return 0;
    }

    private int obtenerPosicionCarrera(int idCarrera) {
        for (int i = 0; i < carreras.size(); i++) {
            if (carreras.get(i).getIdCarrera() == idCarrera)
                return i;
        }
        return 0;
    }
}
