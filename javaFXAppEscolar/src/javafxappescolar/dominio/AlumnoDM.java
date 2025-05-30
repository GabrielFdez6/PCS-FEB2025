package javafxappescolar.dominio;

import javafxappescolar.modelo.dao.AlumnoDAO;
import javafxappescolar.modelo.pojo.ResultadoOperacion;

import java.sql.SQLException;

public class AlumnoDM {

    public static ResultadoOperacion verificarEstadoMatricula(String matricula) {
        ResultadoOperacion resultado = new ResultadoOperacion();
        if (matricula.startsWith("s")) {
            try {
                boolean existe = AlumnoDAO.verificarExistenciaMatricula(matricula);
                resultado.setError(existe);
                if (existe) {
                    resultado.setMensaje("La matricula ya existe en los registros");
                }
            } catch (SQLException e) {
                resultado.setError(true);
                resultado.setMensaje("Por el momento no se puede validar la matrícula, inténtelo más tarde");
            }
        } else {
            resultado.setError(true);
            resultado.setMensaje("La matrícula no tiene el formato correcto");
        }
        return resultado;
    }
}
