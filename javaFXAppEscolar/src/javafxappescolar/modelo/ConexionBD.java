package javafxappescolar.modelo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author gabriel-fdez
 */
public class ConexionBD {
    
    private static final String IP = "localhost";
    private static final String PUERTO = "3306";
    private static final String NOMBRE_BD = "escolar";
    private static final String USUARIO = "root";
    private static final String PASSWORD = "G1212";
    private static final String DRIVER = "com.mysql.jdbc.Driver";
    
    public static Connection abrirConexion() {
        Connection conexionBD = null;
        String urlConexion = 
                String.format("jdbc:mysql://%s:%s/%s?allowPublicKeyRetrieval=true&useSSL=false",
                IP, PUERTO, NOMBRE_BD);
        
        try {
            Class.forName(DRIVER);
            conexionBD = DriverManager.getConnection(urlConexion, USUARIO, PASSWORD);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.err.println("Error Clase no encontrada.");
        } catch (SQLException s) {    
            s.printStackTrace();
            System.err.println("Error en la conexión: " + s.getMessage());
        }        
        return conexionBD;
    }
}
