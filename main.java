import java.util.Scanner;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;


public class main {

    // Colores ANSI 

    static String RESET = "\u001B[0m";
    static String GRIS    = "\u001B[90m"; // pared
    static String BLANCO  = "\u001B[37m"; // pasillo
    static String VERDE   = "\u001B[92m"; // salida
    static String ROJO    = "\u001B[91m"; // obstaculo
    static String AZUL    = "\u001B[94m"; // estudiante
    static String AMARILLO = "\u001B[93m"; // profesor
    static String MAGENTA = "\u001B[95m"; // mantenimiento 

    //Simbolos consola
    static char PARED = '#';
    static char PASILLO = '_';
    static char SALIDA = 'S';
    static char OBSTACULO = 'X';
    static char ESTUDIANTE = 'E';
    static char PROFESOR = 'P';
    static char MANTENIMIENTO = 'M';

    public static void main(String[] args) {
        String opcion = menuPrincipal();
        if (opcion.equals("salir")) {
            System.out.println("Saliendo del programa...");
            return;
        }
        char[][] mapa = crearMapa(10, 15);
        
        // Carga los usuarios de prueba de la Fase 1
         List<Map<String, Object>> usuariosPrueba = crearUsuariosFase1();

        //imprimirMapa(mapa, usuariosPrueba); fase 2 
        simularTurnos(mapa, usuariosPrueba);

}
    public static void simularTurnos(char[][] mapa, List<Map<String, Object>> usuarios) {
        Scanner scanner = new Scanner(System.in);
        int turno = 1;
        boolean continuando = true;

        System.out.println("\n--- INICIANDO SIMULACIÓN POR TURNOS ---");
        System.out.println("Presiona ENTER para avanzar al siguiente turno (o escribe 's' para salir).\n");

        while (continuando) {
            System.out.println(AMARILLO + "=== TURNO " + turno + " ===" + RESET);
            imprimirMapa(mapa, usuarios);

            System.out.print("Presiona ENTER para el siguiente turno ['s' para salir]: ");
            String entrada = scanner.nextLine();

            if (entrada.equalsIgnoreCase("s") || entrada.equalsIgnoreCase("salir")) {
                continuando = false;
                System.out.println("Fin de la simulación de turnos.");
            } else {
                turno++;
                System.out.println();
            }
        }
    }



    // Solo para las pruebas del main() de este archivo
    private static Map<String, Object> crearUsuarioPrueba(int id, String rol, int x, int y) {
        Map<String, Object> u = new HashMap<>();
        u.put("id", id);
        u.put("rol", rol);
        u.put("x", x);
        u.put("y", y);
        return u;
    }

    // Crear lista con 4 diccionarios de prueba para la Fase 1
    public static List<Map<String, Object>> crearUsuariosFase1() {
        List<Map<String, Object>> usuarios = new ArrayList<>();
        
        // 2 estudiantes
        usuarios.add(crearUsuarioPrueba(1, "E", 3, 2));
        usuarios.add(crearUsuarioPrueba(2, "E", 4, 5));
        
        // 1 profesor
        usuarios.add(crearUsuarioPrueba(3, "P", 7, 5));
        
        // 1 mantenimiento
        usuarios.add(crearUsuarioPrueba(4, "M", 8, 8));
        
        return usuarios;
    }

//Crear mapa con paredes y pasillos
public static char[][] crearMapa(int filas, int columnas){
    char[][] mapa = new char[filas][columnas];
    for (int f=0;f<filas;f++){
        for (int c=0;c<columnas;c++){ //Si la celda esta en el borte del mapa, lo pone como pared, si no poner pasillo
            if (f==0 || f==filas-1 || c==0 || c==columnas-1){
                mapa[f][c] = PARED;
            } else {
                mapa[f][c] = PASILLO;
            }
        }
    }
    //Crea las salidas en el centro de la primera y ultima fila
    int centro =  columnas/2;
    mapa[0][centro] = SALIDA;
    mapa[filas-1][centro] = SALIDA;
    return mapa;
}

public static void imprimirMapa(char[][] mapa, List<Map<String,Object>>usuarios){
    HashMap< String, Map<String, Object>> posiciones = new HashMap<>();   
       for (Map<String, Object> u : usuarios) {
            int x = (int) u.get("x");
            int y = (int) u.get("y");
            posiciones.put(y + "," + x, u);
        }
 
        int filas = mapa.length;
        int columnas = mapa[0].length;
        StringBuilder pantalla = new StringBuilder();
 
        for (int f = 0; f < filas; f++) {
            for (int c = 0; c < columnas; c++) {
                String clave = f + "," + c;
                if (posiciones.containsKey(clave)) {
                    pantalla.append(dibujarUsuario(posiciones.get(clave)));
                } else {
                    pantalla.append(dibujarCelda(mapa[f][c]));
                }
            }
            pantalla.append("\n");
        }
        System.out.print(pantalla);
        System.out.println(RESET);

    }
        private static String dibujarCelda(char simbolo) {
        if (simbolo == PARED) return GRIS + simbolo + RESET;
        if (simbolo == SALIDA) return VERDE + simbolo + RESET;
        if (simbolo == OBSTACULO) return ROJO + simbolo + RESET;
        return BLANCO + simbolo + RESET;
    }
 
    private static String dibujarUsuario(Map<String, Object> usuario) {
        String rol = (String) usuario.get("rol");
        String color;
        char simbolo;
        switch (rol) {
            case "E": color = AZUL; simbolo = ESTUDIANTE; break;
            case "P": color = AMARILLO; simbolo = PROFESOR; break;
            case "M": color = MAGENTA; simbolo = MANTENIMIENTO; break;
            default:  color = RESET; simbolo = '?';
        }
        return color + simbolo + RESET;
    }

    public static String menuPrincipal() {
    Scanner leer = new Scanner(System.in);
    int opcion;
    while (true) {
        System.out.println("    SMARTSCHOOL    ");
        System.out.println("1. Iniciar simulacion");
        System.out.println("2. Ver reglas");
        System.out.println("3. Salir");
        System.out.print("Seleccione una opcion: ");
        try {
            opcion = leer.nextInt();
            switch (opcion) {
                case 1: return "iniciar";
                case 2: return "reglas";
                case 3: return "salir";
                default: System.out.println("Opcion invalida");
            }
        } catch (Exception e) {
            System.out.println("Opcion invalida");
            leer.nextLine(); 
        }
    }
}
 public static void pantallaResultados(Map<String, Object> stats) {
        int evacuados = (int) stats.get("evacuados");
        int total = (int) stats.get("total");
        double porcentaje = total > 0 ? (evacuados * 100.0 / total) : 0;
 
        String mensaje;
        String color;
        if (porcentaje >= 90) {
            mensaje = "VICTORIA TOTAL - Flujo perfecto";
            color = VERDE;
        } else if (porcentaje >= 60) {
            mensaje = "RESULTADO MEDIO - Hubo demoras o atascos";
            color = AMARILLO;
        } else {
            mensaje = "FRACASO - Pasillos colapsados";
            color = ROJO;
        }
 
        System.out.println(color + "=".repeat(40) + RESET);
        System.out.println("Estudiantes evacuados: " + evacuados + "/" + total);
        System.out.printf("Porcentaje de exito: %.1f%%%n", porcentaje);
        System.out.println(color + mensaje + RESET);
        System.out.println(color + "=".repeat(40) + RESET);
    }
    

}
