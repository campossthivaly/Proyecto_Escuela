/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
 
//Creado por: Allan Castro, Dorian Castro y Sthivaly Campos
package fase1;
 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Random;
 
public class escuela {
 
    // Colores ANSI 
    static String RESET = "\u001B[0m";
    static String GRIS    = "\u001B[90m"; // pared
    static String BLANCO  = "\u001B[37m"; // pasillo
    static String VERDE   = "\u001B[92m"; // salida
    static String ROJO    = "\u001B[91m"; // obstaculo
    static String AZUL    = "\u001B[94m"; // estudiante
    static String AMARILLO = "\u001B[93m"; // profesor
    static String MAGENTA = "\u001B[95m"; // mantenimiento 
 
    // Simbolos consola
    static char PARED = '#';
    static char PASILLO = '_';
    static char SALIDA = 'S';
    static char OBSTACULO = 'X';
    static char ESTUDIANTE = 'E';
    static char PROFESOR = 'P';
    static char MANTENIMIENTO = 'M';
 
    // Scanner único compartido por todo el programa, para no abrir varios sobre System.in
    static Scanner scannerGlobal = new Scanner(System.in);
 
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String opcion;
        boolean jugarDeNuevo = true;
 
        while (jugarDeNuevo) {
            do {
                opcion = menuPrincipal();
 
                if (opcion.equals("reglas")) {
                    mostrarReglas();
                }
            } while (opcion.equals("reglas"));
 
            if (opcion.equals("salir")) {
                System.out.println("Saliendo del programa...");
                jugarDeNuevo = false;
                continue;
            }
 
            char[][] mapa = crearMapa(10, 15);
            List<Map<String, Object>> usuariosPrueba = crearUsuariosFase1();
            integrarDatosMovimiento(usuariosPrueba);
 
            int maxTurnos = elegirNumeroTurnos();
            simularTurnos(mapa, usuariosPrueba, maxTurnos);
        }
 
        scannerGlobal.close();
    }
 
    public static void mostrarReglas() {
        System.out.println(VERDE + "=".repeat(40) + RESET);
        System.out.println("REGLAS DEL JUEGO");
        System.out.println(VERDE + "=".repeat(40) + RESET);
        System.out.println("- Los estudiantes se mueven solos hacia su salida.");
        System.out.println("- Los profesores patrullan al azar.");
        System.out.println("- Mantenimiento repara obstaculos/puertas danadas.");
        System.out.println("- Pueden aparecer obstaculos aleatorios en los pasillos.");
        System.out.println("- Si 3+ estudiantes quedan juntos por 2+ turnos, salta una alarma de congestion.");
        System.out.println("- Al final se calcula el % de estudiantes evacuados:");
        System.out.println("  90-100% Victoria Total | 60-89% Resultado Medio | <60% Fracaso");
        System.out.println(VERDE + "=".repeat(40) + RESET);
        System.out.println();
    }
 
    // Menú para que el usuario elija cuántos turnos durará la partida
    public static int elegirNumeroTurnos() {
        int opcion;
        while (true) {
            System.out.println("\n¿Cuántos turnos quieres jugar?");
            System.out.println("1. Partida corta (8 turnos)");
            System.out.println("2. Partida media (15 turnos)");
            System.out.println("3. Partida larga (25 turnos)");
            System.out.print("Selecciona una opción: ");
            try {
                opcion = scannerGlobal.nextInt();
                scannerGlobal.nextLine(); // limpia el salto de línea pendiente
                switch (opcion) {
                    case 1: return 8;
                    case 2: return 15;
                    case 3: return 25;
                    default: System.out.println("Opción inválida");
                }
            } catch (Exception e) {
                System.out.println("Opción inválida");
                scannerGlobal.nextLine();
            }
        }
    }
 
    public static void simularTurnos(char[][] mapa, List<Map<String, Object>> usuarios, int maxTurnos) {
        int turno = 1;
        boolean continuando = true;
 
        // Diccionario para recordar cuántos turnos lleva una congestión activa
        Map<String, Integer> registroCongestion = new HashMap<>();
 
        System.out.println("\n--- INICIANDO SIMULACIÓN POR TURNOS ---");
 
        // --- Mensaje inicial con el total de estudiantes a evacuar ---
        int totalEstudiantesInicial = 0;
        for (Map<String, Object> u : usuarios) {
            if (u.get("rol").equals("E")) {
                totalEstudiantesInicial++;
            }
        }
        System.out.println(AZUL + "Hay " + totalEstudiantesInicial + " estudiante(s) que deben evacuar." + RESET);
 
        while (continuando && turno <= maxTurnos) {
            System.out.println(AMARILLO + "=== TURNO " + turno + " ===" + RESET);
 
            eventoAleatorioObstaculo(mapa, usuarios);
            eventoCondicionalAlarma(usuarios, registroCongestion);
 
            // Compañero 1 dibuja la matriz
            imprimirMapa(mapa, usuarios);
 
            // --- LÓGICA DE FIN DE JUEGO (Compañero 3) ---
            int totalEstudiantes = 0;
            int estudiantesEvacuados = 0;
 
            for (Map<String, Object> u : usuarios) {
                if (u.get("rol").equals("E")) {
                    totalEstudiantes++;
                    // Si el estudiante ya no está activo, significa que salió
                    if (!(boolean) u.get("activo")) {
                        estudiantesEvacuados++;
                    }
                }
            }
 
            // Evaluar victoria si todos salieron
            if (totalEstudiantes > 0 && estudiantesEvacuados == totalEstudiantes) {
                System.out.println(VERDE + "\n¡Todos los estudiantes han sido evacuados!" + RESET);
                Map<String, Object> stats = calcularVictoria(estudiantesEvacuados, totalEstudiantes);
                pantallaResultados(stats);
                break; // Rompe el ciclo while
            }
 
            // Llamada a tu función accionUsuario
            String entrada = accionUsuario(scannerGlobal);
 
            if (entrada.equalsIgnoreCase("s") || entrada.equalsIgnoreCase("salir")) {
                continuando = false;
                System.out.println("Simulación abortada manualmente.");
            } else {
                turno++;
                System.out.println();
                moverTodos(usuarios, mapa);
            }
        }
 
        // Evaluar victoria si se acabaron los turnos
        if (turno > maxTurnos) {
            System.out.println(ROJO + "\n¡Se agotaron los turnos!" + RESET);
 
            int totalEstudiantes = 0;
            int estudiantesEvacuados = 0;
 
            for (Map<String, Object> u : usuarios) {
                if (u.get("rol").equals("E")) {
                    totalEstudiantes++;
                    if (!(boolean) u.get("activo")) {
                        estudiantesEvacuados++;
                    }
                }
            }
 
            Map<String, Object> stats = calcularVictoria(estudiantesEvacuados, totalEstudiantes);
            pantallaResultados(stats);
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
 
        // 3 estudiantes juntos para probar la alarma
        usuarios.add(crearUsuarioPrueba(1, "E", 3, 2));
        usuarios.add(crearUsuarioPrueba(2, "E", 3, 3));
        usuarios.add(crearUsuarioPrueba(5, "E", 4, 2));
 
        // 1 profesor
        usuarios.add(crearUsuarioPrueba(3, "P", 7, 5));
 
        // 1 mantenimiento
        usuarios.add(crearUsuarioPrueba(4, "M", 8, 8));
 
        return usuarios;
    }
 
    // Integración de datos para el movimiento de usuarios
    public static void integrarDatosMovimiento(List<Map<String, Object>> usuarios) {
        for (Map<String, Object> u : usuarios) {
            int id = (int) u.get("id");
            u.put("activo", true);
 
            if (id == 1) {
                u.put("meta_x", 7);
                u.put("meta_y", 0);
            } else if (id == 2) {
                u.put("meta_x", 7);
                u.put("meta_y", 9);
            } else if (id == 5) {
                u.put("meta_x", 7);
                u.put("meta_y", 9);
            } else if (id == 3) {
                u.put("meta_x", 7);
                u.put("meta_y", 5);
            } else if (id == 4) {
                u.put("meta_x", 7);
                u.put("meta_y", 5);
            }
        }
    }
 
    // ----------------------------------------------------------------
    // --- CÓDIGO DEL MOVIMIENTO DE USUARIOS ---
    // ----------------------------------------------------------------
 
    // 1. Crear usuario actualizado (ahora incluye la meta y estado activo)
    public static Map<String, Object> crearUsuario(int id, String rol, int x, int y, int metaX, int metaY) {
        Map<String, Object> u = new HashMap<>();
        u.put("id", id);
        u.put("rol", rol);
        u.put("x", x);       // Columna actual
        u.put("y", y);       // Fila actual
        u.put("meta_x", metaX); // Columna destino
        u.put("meta_y", metaY); // Fila destino
        u.put("activo", true);  // Para saber si ya evacuó
        return u;
    }
 
    // 2. Movimiento de estudiantes (Buscan la salida)
    public static void moverEstudiantes(List<Map<String, Object>> usuarios, char[][] mapa) {
        for (Map<String, Object> u : usuarios) {
            if (u.get("rol").equals("E") && (boolean) u.get("activo")) {
                int x = (int) u.get("x");
                int y = (int) u.get("y");
                int metaX = (int) u.get("meta_x");
                int metaY = (int) u.get("meta_y");
 
                // Lógica matemática básica (1 paso a la vez)
                int difX = Integer.compare(metaX, x); // Retorna 1, -1 o 0
                int difY = Integer.compare(metaY, y); // Retorna 1, -1 o 0
 
                // Intenta moverse en X primero, si no puede, intenta en Y
                if (difX != 0 && esPosicionValida(x + difX, y, mapa)) {
                    u.put("x", x + difX);
                } else if (difY != 0 && esPosicionValida(x, y + difY, mapa)) {
                    u.put("y", y + difY);
                } else {
                    // Si el camino principal está bloqueado, busca una alternativa
                    moverEstudianteAlternativa(u, x, y, difX, difY, mapa);
                }
 
                // Revisar si llegó a la salida
                int nuevoX = (int) u.get("x");
                int nuevoY = (int) u.get("y");
                if (mapa[nuevoY][nuevoX] == SALIDA) {
                    u.put("activo", false); // El estudiante salió del mapa
 
                    // --- MENSAJE DE EVACUACIÓN ---
                    int id = (int) u.get("id");
                    int faltan = 0;
                    for (Map<String, Object> otro : usuarios) {
                        if (otro.get("rol").equals("E") && (boolean) otro.get("activo")) {
                            faltan++;
                        }
                    }
                    System.out.println(VERDE + "¡El estudiante #" + id + " ha salido! Faltan "
                            + faltan + " estudiante(s) por evacuar." + RESET);
                }
            }
        }
    }
 
    // Movimiento alternativo cuando el estudiante encuentra un obstáculo
    private static void moverEstudianteAlternativa(
            Map<String, Object> u,
            int x,
            int y,
            int difX,
            int difY,
            char[][] mapa) {
 
        // Primero intenta moverse en la dirección contraria a X
        if (difX != 0 && esPosicionValida(x - difX, y, mapa)) {
            u.put("x", x - difX);
        } 
        // Luego intenta moverse en la dirección contraria a Y
        else if (difY != 0 && esPosicionValida(x, y - difY, mapa)) {
            u.put("y", y - difY);
        } 
        // Si tampoco puede, intenta moverse hacia arriba
        else if (esPosicionValida(x, y - 1, mapa)) {
            u.put("y", y - 1);
        } 
        // Si no puede subir, intenta moverse hacia abajo
        else if (esPosicionValida(x, y + 1, mapa)) {
            u.put("y", y + 1);
        } 
        // Si no puede verticalmente, intenta moverse hacia la izquierda
        else if (esPosicionValida(x - 1, y, mapa)) {
            u.put("x", x - 1);
        } 
        // Finalmente intenta moverse hacia la derecha
        else if (esPosicionValida(x + 1, y, mapa)) {
            u.put("x", x + 1);
        }
    }
 
    // 3. Movimiento de Profesores (Patrullan aleatoriamente en esta fase)
    public static void moverProfesores(List<Map<String, Object>> usuarios, char[][] mapa) {
        for (Map<String, Object> u : usuarios) {
            if (u.get("rol").equals("P") && (boolean) u.get("activo")) {
                int x = (int) u.get("x");
                int y = (int) u.get("y");
 
                if (u.containsKey("meta_x") && u.containsKey("meta_y")) {
                    int metaX = (int) u.get("meta_x");
                    int metaY = (int) u.get("meta_y");
                    int difX = Integer.compare(metaX, x);
                    int difY = Integer.compare(metaY, y);
 
                    if (difX != 0 && esPosicionValida(x + difX, y, mapa)) u.put("x", x + difX);
                    else if (difY != 0 && esPosicionValida(x, y + difY, mapa)) u.put("y", y + difY);
 
                    // Si llegó a la meta, se borra para que vuelva a patrullar
                    if ((int)u.get("x") == metaX && (int)u.get("y") == metaY) {
                        u.remove("meta_x");
                        u.remove("meta_y");
                    }
                } else {
                    // Patrullaje libre normal
                    int difX = (int) (Math.random() * 3) - 1;
                    int difY = (int) (Math.random() * 3) - 1;
                    if (esPosicionValida(x + difX, y + difY, mapa) && mapa[y + difY][x + difX] != SALIDA) {
                        u.put("x", x + difX);
                        u.put("y", y + difY);
                    }
                }
            }
        }
    }
 
    // 4. Movimiento de Mantenimiento (Por ahora se mueve hacia el centro o un punto fijo)
    public static void moverMantenimiento(List<Map<String, Object>> usuarios, char[][] mapa) {
        for (Map<String, Object> u : usuarios) {
            if (u.get("rol").equals("M") && (boolean) u.get("activo")) {
                int x = (int) u.get("x");
                int y = (int) u.get("y");
                int metaX = (int) u.get("meta_x");
                int metaY = (int) u.get("meta_y");
 
                int difX = Integer.compare(metaX, x);
                int difY = Integer.compare(metaY, y);
 
                if (difY != 0 && mapa[y + difY][x] == OBSTACULO) {
                    mapa[y + difY][x] = PASILLO;
                    System.out.println(MAGENTA + "¡Mantenimiento ha limpiado un obstáculo!" + RESET);
                }
                // Revisar si el siguiente paso en X es el obstáculo para limpiarlo
                else if (difX != 0 && mapa[y][x + difX] == OBSTACULO) {
                    mapa[y][x + difX] = PASILLO;
                    System.out.println(MAGENTA + "¡Mantenimiento ha limpiado un obstáculo!" + RESET);
                }
 
                // Mantenimiento intenta llegar a su meta
                if (difY != 0 && esPosicionValida(x, y + difY, mapa)) {
                    u.put("y", y + difY);
                } else if (difX != 0 && esPosicionValida(x + difX, y, mapa)) {
                    u.put("x", x + difX);
                }
            }
        }
    }
 
    // Función auxiliar para validar que no se salgan ni choquen con paredes
    private static boolean esPosicionValida(int x, int y, char[][] mapa) {
        // Verifica límites del mapa
        if (y >= 0 && y < mapa.length && x >= 0 && x < mapa[0].length) {
            // No puede pisar paredes ni obstáculos
            return mapa[y][x] != PARED && mapa[y][x] != OBSTACULO;
        }
        return false;
    }
 
    // Función maestra que agrupa todos los movimientos para que el Compañero 3 la llame
    public static void moverTodos(List<Map<String, Object>> usuarios, char[][] mapa) {
        moverEstudiantes(usuarios, mapa);
        moverProfesores(usuarios, mapa);
        moverMantenimiento(usuarios, mapa);
    }
 
    // Crear mapa con paredes y pasillos
    public static char[][] crearMapa(int filas, int columnas){
        char[][] mapa = new char[filas][columnas];
        for (int f=0;f<filas;f++){
            for (int c=0;c<columnas;c++){ // Si la celda esta en el borde del mapa, lo pone como pared, si no pone pasillo
                if (f==0 || f==filas-1 || c==0 || c==columnas-1){
                    mapa[f][c] = PARED;
                } else {
                    mapa[f][c] = PASILLO;
                }
            }
        }
 
        // Crea las salidas en el centro de la primera y ultima fila
        int centro = columnas/2;
        mapa[0][centro] = SALIDA;
        mapa[filas-1][centro] = SALIDA;
        return mapa;
    }
 
    public static void imprimirMapa(char[][] mapa, List<Map<String,Object>>usuarios){
        HashMap<String, List<Map<String, Object>>> posiciones = new HashMap<>();
        for (Map<String, Object> u : usuarios) {
            int x = (int) u.get("x");
            int y = (int) u.get("y");
            String clave = y + "," + x;
            posiciones.computeIfAbsent(clave, k -> new ArrayList<>()).add(u);
        }
 
        int filas = mapa.length;
        int columnas = mapa[0].length;
        StringBuilder pantalla = new StringBuilder();
        List<String> ocultos = new ArrayList<>(); // avisos de estudiantes tapados por otro personaje
 
        for (int f = 0; f < filas; f++) {
            for (int c = 0; c < columnas; c++) {
                String clave = f + "," + c;
                List<Map<String, Object>> aqui = posiciones.get(clave);
 
                // Solo nos interesan los usuarios activos en esta celda
                List<Map<String, Object>> activosAqui = new ArrayList<>();
                if (aqui != null) {
                    for (Map<String, Object> u : aqui) {
                        if ((boolean) u.getOrDefault("activo", true)) {
                            activosAqui.add(u);
                        }
                    }
                }
 
                if (!activosAqui.isEmpty()) {
                    // Solo se dibuja el primero; si hay más, se quedan ocultos visualmente
                    pantalla.append(dibujarUsuario(activosAqui.get(0)));
 
                    if (activosAqui.size() > 1) {
                        for (int i = 1; i < activosAqui.size(); i++) {
                            Map<String, Object> oculto = activosAqui.get(i);
                            if (oculto.get("rol").equals("E")) {
                                ocultos.add("El estudiante #" + oculto.get("id")
                                        + " sigue ahí, pero está tapado por otro personaje en ("
                                        + f + ", " + c + ").");
                            }
                        }
                    }
                } else {
                    pantalla.append(dibujarCelda(mapa[f][c]));
                }
            }
            pantalla.append("\n");
        }
 
        System.out.print(pantalla);
        System.out.println(RESET);
 
        // Avisos de estudiantes que quedaron tapados por otro personaje en la misma celda
        for (String msg : ocultos) {
            System.out.println(AMARILLO + "⚠ " + msg + RESET);
        }
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
        int opcion;
        while (true) {
            System.out.println("    SMARTSCHOOL    ");
            System.out.println("1. Iniciar simulacion");
            System.out.println("2. Ver reglas");
            System.out.println("3. Salir");
            System.out.print("Seleccione una opcion: ");
            try {
                opcion = scannerGlobal.nextInt();
                switch (opcion) {
                    case 1: return "iniciar";
                    case 2: return "reglas";
                    case 3: return "salir";
                    default: System.out.println("Opcion invalida");
                }
            } catch (Exception e) {
                System.out.println("Opcion invalida");
                scannerGlobal.nextLine(); 
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
 
    public static Map<String, Object> calcularVictoria(int evacuados, int total) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("evacuados", evacuados);
        stats.put("total", total);
        return stats;
    }
 
    // --- MÉTODOS DEL COMPAÑERO 3 ---
 
    // 1. Evento Aleatorio: Aparece un obstáculo en el pasillo
    public static void eventoAleatorioObstaculo(char[][] mapa,List<Map<String, Object>> usuarios) {
        Random rand = new Random();
        int filas = mapa.length;
        int columnas = mapa[0].length;
 
        // Probabilidad del 30% de que aparezca un obstáculo en este turno
        if (rand.nextInt(100) < 30) {
            // Intentamos buscar un pasillo vacío al azar (máximo 10 intentos para no ciclar el programa)
            for (int i = 0; i < 10; i++) {
                int f = rand.nextInt(filas);
                int c = rand.nextInt(columnas);
 
                // Si la celda es un pasillo, metemos el obstáculo y avisamos
                if (mapa[f][c] == PASILLO) {
                    mapa[f][c] = OBSTACULO;
                    System.out.println(ROJO + "¡EVENTO! Un obstáculo inesperado ha aparecido en (" + f + ", " + c + ")." + RESET);
 
                    // --- SOLUCIÓN RÁPIDA: Mandar a Mantenimiento directo al obstáculo ---
                    for (Map<String, Object> u : usuarios) {
                        if (u.get("rol").equals("M")) {
                            u.put("meta_x", c);
                            u.put("meta_y", f);
                            break; // Ya le dimos la orden, salimos del ciclo
                        }
                    }
 
                    break;
                }
            }
        }
    }
 
    // 2. Evento Condicional: Alarma de congestión (3+ estudiantes juntos)
    public static void eventoCondicionalAlarma(List<Map<String, Object>> usuarios, Map<String, Integer> registroCongestion) {
        // En esta Fase 2, haremos una validación básica de cercanía entre estudiantes.
        int estudiantesCercanos = 0;
 
        for (Map<String, Object > u1 : usuarios) {
            if (!u1.get("rol").equals("E")) continue;
 
            estudiantesCercanos = 0;
            int x1 = (int) u1.get("x");
            int y1 = (int) u1.get("y");
 
            for (Map<String, Object> u2 : usuarios) {
                if (!u2.get("rol").equals("E")) continue;
 
                int x2 = (int) u2.get("x");
                int y2 = (int) u2.get("y");
 
                // Si están a 1 o 0 casillas de distancia, se consideran "juntos"
                if (Math.abs(x1 - x2) <= 1 && Math.abs(y1 - y2) <= 1) {
                    estudiantesCercanos++;
                }
            }
 
            String zona = x1 + "," + y1;
 
            // Si hay 3 o más estudiantes (el PDF pide 3+ juntos)
            if (estudiantesCercanos >= 3) {
                int turnos = registroCongestion.getOrDefault(zona, 0) + 1;
                registroCongestion.put(zona, turnos);
 
                // Si llevan más de 2 turnos juntos, salta la alarma
                if (turnos > 2) {
                    System.out.println(AMARILLO + "¡ALARMA DE CONGESTIÓN! Demasiados estudiantes atascados cerca de (" + x1 + ", " + y1 + ")." + RESET);
                    for (Map<String, Object> u3 : usuarios) {
                        if (u3.get("rol").equals("P")) {
                            u3.put("meta_x", x1);
                            u3.put("meta_y", y1);
                            break;
                        }
                    }
                }
            } else {
                registroCongestion.remove(zona); // Se despejó la zona
            }
        }
    }
 
    public static String accionUsuario(Scanner scanner) {
        System.out.print("Presiona ENTER para el siguiente turno ['s' para salir]: ");
        return scanner.nextLine();
    }
}