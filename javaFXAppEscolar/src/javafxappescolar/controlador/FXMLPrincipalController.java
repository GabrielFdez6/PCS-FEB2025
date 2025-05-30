package javafxappescolar.controlador;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafxappescolar.JavaFXAppEscolar;
import javafxappescolar.modelo.pojo.Usuario;

/**
 * FXML Controller class
 *
 * @author gabriel-fdez
 */
public class FXMLPrincipalController implements Initializable {

    private Usuario usuarioSesion;
    @FXML
    private Label lbNombre;
    @FXML
    private Label lbUsuario;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    public void iniciarlizarInformacion(Usuario usuarioSesion) {
        this.usuarioSesion = usuarioSesion;
        cargarInformacionUsuario();
    }

    private void cargarInformacionUsuario() {
        if (usuarioSesion != null) {
            lbNombre.setText(usuarioSesion.toString());
            lbUsuario.setText(usuarioSesion.getUsername());
        }
    }

    @FXML
    private void btnClicCerrarSesion(ActionEvent event) {
        try {
            Stage escenario = (Stage) lbNombre.getScene().getWindow();

            FXMLLoader cargador = new FXMLLoader(javafxappescolar.JavaFXAppEscolar.class.getResource("vista/FXMLInicioSesion.fxml"));
            Parent vistaLogin = cargador.load();

            Scene escenaLogin = new Scene(vistaLogin);
            escenario.setScene(escenaLogin);
            escenario.setTitle("Inicio de sesión");
            escenario.show();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void clicBtnAdminAlumnos(ActionEvent event) {
        try {
            Stage escenarioAdmin = new Stage();
            Parent vista = FXMLLoader.load(JavaFXAppEscolar.class.getResource("vista/FXMLAdminAlumno.fxml"));
            Scene escena = new Scene(vista);
            escenarioAdmin.setScene(escena);
            escenarioAdmin.setTitle("Administrador de Alumnos");
            escenarioAdmin.initModality(Modality.APPLICATION_MODAL);
            escenarioAdmin.showAndWait();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
