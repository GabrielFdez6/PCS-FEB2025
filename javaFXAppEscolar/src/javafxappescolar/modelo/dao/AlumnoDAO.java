package javafxappescolar.modelo.dao;

import javafxappescolar.modelo.ConexionBD;
import javafxappescolar.modelo.pojo.Alumno;
import javafxappescolar.modelo.pojo.ResultadoOperacion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * @author gabriel-fdez
 */
public class AlumnoDAO {

    public static ArrayList<Alumno> obtenerAlumnos() throws SQLException {
        ArrayList<Alumno> alumnos = new ArrayList();
        Connection conexionBD = ConexionBD.abrirConexion();

        if (conexionBD != null) {
            String consulta = "SELECT idAlumno, a.nombre, apellidoPaterno, apellidoMaterno, matricula, email, a.idCarrera, fechaNacimiento," +
                    " c.nombre AS 'carrera', c.idFacultad, f.nombre AS 'facultad'" +
                    " FROM alumno a" +
                    " INNER JOIN carrera c ON c.idCarrera = a.idCarrera" +
                    " INNER JOIN facultad f on f.idFacultad = c.idFacultad";
            PreparedStatement sentencia = conexionBD.prepareStatement(consulta);
            ResultSet resultado = sentencia.executeQuery();
            while (resultado.next()) {
                alumnos.add(convertirRegistroAlumno(resultado));
            }
            sentencia.close();
            resultado.close();
            conexionBD.close();
        } else {
            throw new SQLException("Error sin conexión a la Base de datos.");
        }
        return alumnos;
    }

    public static ResultadoOperacion registrarAlumno(Alumno alumno) throws SQLException {
        ResultadoOperacion resultado = new ResultadoOperacion();
        Connection conexionBD = ConexionBD.abrirConexion();
        if (conexionBD != null) {
            String sentencia = "INSERT INTO alumno (nombre, apellidoPaterno, apellidoMaterno, matricula, email, idCarrera, fechaNacimiento, foto)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement prepararSentencia = conexionBD.prepareStatement(sentencia);
            prepararSentencia.setString(1, alumno.getNombre());
            prepararSentencia.setString(2, alumno.getApellidoPaterno());
            prepararSentencia.setString(3, alumno.getApellidoMaterno());
            prepararSentencia.setString(4, alumno.getMatricula());
            prepararSentencia.setString(5, alumno.getEmail());
            prepararSentencia.setInt(6, alumno.getIdCarrera());
            prepararSentencia.setString(7, alumno.getFechaNacimiento());
            prepararSentencia.setBytes(8, alumno.getFoto());

            int filasAfectadas = prepararSentencia.executeUpdate();
            if (filasAfectadas == 1) {
                resultado.setError(false);
                resultado.setMensaje("Alumno(a) registrado correctamente.");
            } else {
                resultado.setError(true);
                resultado.setMensaje("Lo sentimos, por el momento no se puede registrar al alumnos, por favor inténtelo más tarde.");
            }
            prepararSentencia.close();
            conexionBD.close();
    } else {
        throw new SQLException("Sin conexión a la Base de datos.");
    }
    return resultado;
}
    public static byte[] obtenerFotoAlumno(int idAlumno) throws SQLException {
        byte[] foto = null;
        Connection conexionBD = ConexionBD.abrirConexion();
        if (conexionBD != null) {
            String consulta = "SELECT foto FROM alumno WHERE idAlumno = ?";
            PreparedStatement sentencia = conexionBD.prepareStatement(consulta);
            sentencia.setInt(1, idAlumno);
            ResultSet resultado = sentencia.executeQuery();
            if (resultado.next()) {
                foto = resultado.getBytes("foto");
            }
            resultado.close();
            sentencia.close();
            conexionBD.close();
        } else {
            throw new SQLException("Sin conexión a la BD");
        }
        return foto;
    }

    public static boolean verificarExistenciaMatricula(String matricula) throws SQLException {
        boolean existe = false;
        Connection conexionBD = ConexionBD.abrirConexion();

        if (conexionBD != null) {
            String consultaSQL = "SELECT COUNT(*) FROM alumno WHERE matricula = ?";
            PreparedStatement ps = conexionBD.prepareStatement(consultaSQL);
            ps.setString(1, matricula);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                existe = rs.getInt(1) > 0;
            }

            ps.close();
            rs.close();
            conexionBD.close();
        } else {
            throw new SQLException("Error: Sin conexion a la base de datos");
        }
        return existe;
    }

    public static ResultadoOperacion editarAlumno(Alumno alumno) throws SQLException {
        ResultadoOperacion resultado = new ResultadoOperacion();
        Connection conexionBD = ConexionBD.abrirConexion();
        if (conexionBD != null) {
            String sentencia = "UPDATE alumno SET nombre = ?, apellidoPaterno = ?, apellidoMaterno = ?, " +
                    "matricula = ?, email = ?, idCarrera = ?, fechaNacimiento = ?, foto = ? " +
                    "WHERE idAlumno = ?";
            PreparedStatement prepararSentencia = conexionBD.prepareStatement(sentencia);
            asignarParametrosAlumno(prepararSentencia, alumno, true);
            int filasAfectadas = prepararSentencia.executeUpdate();
            if (filasAfectadas == 1) {
                resultado.setError(false);
                resultado.setMensaje("Alumno(a) actualizado correctamente");
            } else {
                resultado.setError(true);
                resultado.setMensaje("No se pudo actualizar la información del alumno(a)");
            }
            prepararSentencia.close();
            conexionBD.close();
        } else {
            throw new SQLException("Sin conexión a la BD");
        }
        return resultado;
    }

    public static ResultadoOperacion eliminarAlumno(int idAlumno) throws SQLException {
        ResultadoOperacion resultado = new ResultadoOperacion();
        Connection conexionBD = ConexionBD.abrirConexion();

        if (conexionBD != null) {
            String consultaSQL = "DELETE FROM alumno WHERE idAlumno = ?";
            PreparedStatement ps = conexionBD.prepareStatement(consultaSQL);
            ps.setInt(1, idAlumno);

            int filasEliminadas = ps.executeUpdate();

            if (filasEliminadas == 1) {
                resultado.setError(false);
                resultado.setMensaje("Alumno eliminado correctamente");
            } else {
                resultado.setError(true);
                resultado.setMensaje("Lo sentimos no se pudo eliminar el alumno, intente más tarde");
            }

            ps.close();
            conexionBD.close();
        } else {
            throw new SQLException("Error: Sin conexion a la base de datos");
        }
        return resultado;
    }

    private static Alumno convertirRegistroAlumno(ResultSet resultado) throws SQLException {
        Alumno alumno = new Alumno();
        alumno.setIdAlumno(resultado.getInt("idAlumno"));
        alumno.setNombre(resultado.getString("nombre"));
        alumno.setApellidoPaterno(resultado.getString("apellidoPaterno"));
        alumno.setApellidoMaterno(resultado.getString("apellidoMaterno"));
        alumno.setMatricula(resultado.getString("matricula"));
        alumno.setEmail(resultado.getString("email"));
        alumno.setIdCarrera(resultado.getInt("idCarrera"));
        alumno.setCarrera(resultado.getString("carrera"));
        alumno.setIdFacultad(resultado.getInt("idFacultad"));
        alumno.setFacultad(resultado.getString("facultad"));
        alumno.setFechaNacimiento(resultado.getString("fechaNacimiento"));
        return alumno;
    }

    private static void asignarParametrosAlumno(PreparedStatement ps, Alumno alumno, boolean incluirID) throws SQLException {
        ps.setString(1, alumno.getNombre());
        ps.setString(2, alumno.getApellidoPaterno());
        ps.setString(3, alumno.getApellidoMaterno());
        ps.setString(4, alumno.getMatricula());
        ps.setString(5, alumno.getEmail());
        ps.setInt(6, alumno.getIdCarrera());
        ps.setString(7, alumno.getFechaNacimiento());
        ps.setBytes(8, alumno.getFoto());
        if (incluirID) {
            ps.setInt(9, alumno.getIdAlumno());
        }
    }
    
}
