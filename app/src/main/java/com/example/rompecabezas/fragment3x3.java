package com.example.rompecabezas;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.DialogInterface;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.io.InputStream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.*;

public class fragment3x3 extends Fragment {

    private TextView tvA, tvB, tvC, tvD, tvE, tvF, tvG, tvH, tvX, mTextView, tvContador, tvAv, tvBv, tvCv, tvDv, tvEv, tvFv, tvGv, tvHv;
    private TextView[][] matriz;
    private int minutes = 0;
    private Map<String, Drawable> mapaFondos = new HashMap<>();
    private Map<String, Integer> mapaColoresTexto = new HashMap<>();
    private int seconds = 0;
    private int conteo = 0;
    private Runnable timerRunnable;
    private Handler timerHandler;

    private String[] goalState = {"A", "B", "C", "D", "E", "F", "G", "H", "X"};
    private String[] currentState = new String[9];
    private String[] estadoInicial;
    private List<String[]> movimientosManuales = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fragment3x3, container, false);

        // Asignación de TextViews
        tvA = view.findViewById(R.id.tvA);
        tvB = view.findViewById(R.id.tvB);
        tvC = view.findViewById(R.id.tvC);
        tvD = view.findViewById(R.id.tvD);
        tvE = view.findViewById(R.id.tvE);
        tvF = view.findViewById(R.id.tvF);
        tvG = view.findViewById(R.id.tvG);
        tvH = view.findViewById(R.id.tvH);
        tvX = view.findViewById(R.id.tvX);
        //vista
        tvAv = view.findViewById(R.id.tvAv);
        tvBv = view.findViewById(R.id.tvBv);
        tvCv = view.findViewById(R.id.tvCv);
        tvDv = view.findViewById(R.id.tvDv);
        tvEv = view.findViewById(R.id.tvEv);
        tvFv = view.findViewById(R.id.tvFv);
        tvGv = view.findViewById(R.id.tvGv);
        tvHv = view.findViewById(R.id.tvHv);

        //-------------------------
        tvContador = view.findViewById(R.id.tvContador);


        // Obtener referencia al botón btnAtras
        TextView btnAtras = view.findViewById(R.id.btnAtras);

        // Matriz para gestionar los movimientos
        matriz = new TextView[][]{
                {tvA, tvB, tvC},
                {tvD, tvE, tvF},
                {tvG, tvH, tvX}
        };

        // Obtener el Bundle y el ArrayList<byte[]>
        Bundle bundle = getArguments();
        if (bundle != null) {
            ArrayList<byte[]> imageParts = (ArrayList<byte[]>) bundle.getSerializable("imageParts");

            // Cargar las imágenes en los TextView
            if (imageParts != null) {
                cargarImagenParaLaVista(view, imageParts);
                cargarImagen(view, imageParts);
            }
        }
        timerHandler = new Handler();
        mTextView = view.findViewById(R.id.tvReloj);
        startTimer();
        mezclarEstadoInicial();

        // Listeners para movimiento manual
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                matriz[i][j].setOnClickListener(v -> moverPieza((TextView) v));
            }
        }




        // Configurar el OnClickListener para el botón btnAtras
        btnAtras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mostrar un diálogo de confirmación
                mostrarDialogoConfirmacion();
            }
        });

        // Botón para resolver automáticamente
        view.findViewById(R.id.btnArmar).setOnClickListener(v -> {
            // Bloquear interfaz durante la resolución
            view.findViewById(R.id.btnArmar).setEnabled(false);
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    matriz[i][j].setEnabled(false);
                }
            }
            revertirMovimientos();
        });

        return view;
    }
    //pa cargar la imagen
    private void cargarImagenParaLaVista(View view,ArrayList<byte[]> imageParts) {
        // Verificar que hay suficientes partes para los TextView
        if (imageParts != null && imageParts.size() ==9) {
            // Asignar cada parte a un TextView
            tvAv.setBackground(new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(imageParts.get(0), 0, imageParts.get(0).length)));
            tvBv.setBackground(new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(imageParts.get(1), 0, imageParts.get(1).length)));
            tvCv.setBackground(new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(imageParts.get(2), 0, imageParts.get(2).length)));
            tvDv.setBackground(new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(imageParts.get(3), 0, imageParts.get(3).length)));
            tvEv.setBackground(new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(imageParts.get(4), 0, imageParts.get(4).length)));
            tvFv.setBackground(new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(imageParts.get(5), 0, imageParts.get(5).length)));
            tvGv.setBackground(new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(imageParts.get(6), 0, imageParts.get(6).length)));
            tvHv.setBackground(new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(imageParts.get(7), 0, imageParts.get(7).length)));
            Toast.makeText(getActivity(), "No hay suficientes partes de la imagen", Toast.LENGTH_SHORT).show();
        }
    }



    //pa cargar la imagen del puzzle
    private void cargarImagen(View view,ArrayList<byte[]> imageParts) {
        // Verificar que hay suficientes partes para los TextView
        if (imageParts != null && imageParts.size() ==9) {
            // Asignar cada parte a un TextView
            tvA.setBackground(new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(imageParts.get(0), 0, imageParts.get(0).length)));
            tvB.setBackground(new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(imageParts.get(1), 0, imageParts.get(1).length)));
            tvC.setBackground(new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(imageParts.get(2), 0, imageParts.get(2).length)));
            tvD.setBackground(new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(imageParts.get(3), 0, imageParts.get(3).length)));
            tvE.setBackground(new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(imageParts.get(4), 0, imageParts.get(4).length)));
            tvF.setBackground(new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(imageParts.get(5), 0, imageParts.get(5).length)));
            tvG.setBackground(new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(imageParts.get(6), 0, imageParts.get(6).length)));
            tvH.setBackground(new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(imageParts.get(7), 0, imageParts.get(7).length)));
            Toast.makeText(getActivity(), "No hay suficientes partes de la imagen", Toast.LENGTH_SHORT).show();
        }
    }


    //pa ir atras
    private void mostrarDialogoConfirmacion() {
        // Crear un AlertDialog
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirmar")
                .setMessage("¿Estás seguro de que quieres volver al menú principal?")
                .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Cerrar el fragmento actual y regresar al MainActivity
                        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                        fragmentManager.popBackStack(); // Elimina el fragmento de la pila de retroceso
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // No hacer nada, simplemente cerrar el diálogo
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert) // Icono de advertencia
                .show();
    }

    // Inicia el temporizador
    private void startTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                seconds++;
                if (seconds == 60) {
                    seconds = 0;
                    minutes++;
                }

                String time = String.format("%02d:%02d", minutes, seconds);
                mTextView.setText(time);

                timerHandler.postDelayed(this, 1000); // Programar la próxima ejecución
            }
        };
        timerHandler.post(timerRunnable); // Iniciar el temporizador
    }

    // Detiene el temporizador
    private void stopTimer() {
        if (timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable); // Detener el Runnable
        }
    }

    private boolean esResoluble(String[] estado) {
        int inversiones = 0;
        int blankIndex = -1;

        // Find the index of "X"
        for (int i = 0; i < estado.length; i++) {
            if (estado[i].equals("X")) {
                blankIndex = i;
                break;
            }
        }

        // Count inversions ignoring "X"
        for (int i = 0; i < estado.length; i++) {
            if (i == blankIndex) continue; // Skip "X"
            for (int j = i + 1; j < estado.length; j++) {
                if (j == blankIndex) continue; // Skip "X"
                if (estado[i].compareTo(estado[j]) > 0) {
                    inversiones++;
                }
            }
        }

        // Check solvability (inversions even + blank row parity)
        int blankRow = 2 - (blankIndex / 3); // 0-based row from bottom
        return (inversiones % 2 == 0) == (blankRow % 2 == 1); // Standard 8-puzzle rule
    }

    // Mezclar el estado inicial del rompecabezas
    private void mezclarEstadoInicial() {
        // Lista de letras para mezclar (A-H y X)
        List<String> letras = new ArrayList<>(Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "X"));

        // Guardar los fondos de los TextView en el mapa
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                TextView piece = matriz[i][j];
                String texto = piece.getText().toString();
                if (!texto.equals("X")) {
                    // Guardar el fondo en el mapa
                    mapaFondos.put(texto, piece.getBackground());
                }
            }
        }

        // Mezclar las letras hasta que el estado sea resoluble
        do {
            Collections.shuffle(letras); // Mezclar las letras (incluyendo la X)
            currentState = letras.toArray(new String[0]); // Convertir a array
        } while (!esResoluble(currentState)); // Verificar si el estado es resoluble

        // Guardar el estado inicial
        estadoInicial = currentState.clone();

        // Asignar el fondo a cada TextView
        for (int i = 0; i < currentState.length; i++) {
            TextView piece = matriz[i / 3][i % 3]; // Obtener el TextView correspondiente
            String texto = currentState[i]; // Obtener el texto (A-H o X)

            if (texto.equals("X")) {
                // Si es la X, hacerla transparente
                piece.setBackgroundColor(Color.TRANSPARENT);
                piece.setTextColor(Color.TRANSPARENT); // Texto transparente
            } else {
                // Si es una letra, asignar el fondo guardado
                piece.setBackground(mapaFondos.get(texto));
                piece.setTextColor(Color.TRANSPARENT); // Texto transparente
            }

            // Asignar el texto (aunque sea transparente)
            piece.setText(texto);
        }

        // Reiniciar el contador y el temporizador
        conteo = 0;
        tvContador.setText("0");
        stopTimer();
        startTimer();
    }

    // Revertir movimientos manuales
    private void revertirMovimientos() {
        List<String[]> movimientosInvertidos = new ArrayList<>(movimientosManuales);
        Collections.reverse(movimientosInvertidos);

        new Handler().postDelayed(new Runnable() {
            int index = 0;

            @Override
            public void run() {
                if (index < movimientosInvertidos.size()) {
                    ejecutarMovimiento(movimientosInvertidos.get(index));
                    index++;
                    new Handler().postDelayed(this, 500);
                } else {
                    movimientosManuales.clear();
                    resolverRompecabezas();
                }
            }
        }, 500);
    }

    // Resolver con A*
    private void resolverRompecabezas() {
        List<String[]> solution = aStarSolve(currentState);
        if (solution != null) {
            new Handler().postDelayed(new Runnable() {
                int index = 0;

                @Override
                public void run() {
                    if (index < solution.size()) {
                        ejecutarMovimiento(solution.get(index));
                        index++;
                        new Handler().postDelayed(this, 500);
                    } else {
                        // Habilitar interfaz después de resolver
                        requireView().findViewById(R.id.btnArmar).setEnabled(true);
                        for (int i = 0; i < 3; i++) {
                            for (int j = 0; j < 3; j++) {
                                matriz[i][j].setEnabled(true);
                            }
                        }
                    }
                }
            }, 500);
        } else {
            Toast.makeText(requireContext(), "No se pudo resolver", Toast.LENGTH_SHORT).show();
        }
    }

    // Algoritmo A* para resolver el rompecabezas
    private List<String[]> aStarSolve(String[] startState) {
        PriorityQueue<State> openList = new PriorityQueue<>(Comparator.comparingInt(State::getF));
        Set<String> closedList = new HashSet<>();
        Map<String, String[]> cameFrom = new HashMap<>();

        openList.add(new State(startState, 0, heuristica(startState)));

        while (!openList.isEmpty()) {
            State current = openList.poll();
            if (Arrays.equals(current.state, goalState)) {
                return reconstruirRuta(cameFrom, current.state);
            }

            closedList.add(Arrays.toString(current.state));

            for (String[] neighbor : obtenerVecinos(current.state)) {
                if (closedList.contains(Arrays.toString(neighbor))) continue;

                int g = current.g + 1;
                int h = heuristica(neighbor);
                int f = g + h;

                if (!openList.contains(new State(neighbor, f, h))) {
                    openList.add(new State(neighbor, f, h));
                    cameFrom.put(Arrays.toString(neighbor), current.state);
                }
            }
        }
        return null; // No hay solución
    }

    // Función heurística: Distancia de Manhattan
    private int heuristica(String[] state) {
        int distancia = 0;
        for (int i = 0; i < state.length; i++) {
            if (!state[i].equals("X")) {
                int valor = state[i].charAt(0) - 'A';
                int filaActual = i / 3;
                int columnaActual = i % 3;
                int filaObjetivo = valor / 3;
                int columnaObjetivo = valor % 3;
                distancia += Math.abs(filaActual - filaObjetivo) + Math.abs(columnaActual - columnaObjetivo);
            }
        }
        return distancia;
    }

    // Reconstruir la ruta desde el estado inicial al objetivo
    private List<String[]> reconstruirRuta(Map<String, String[]> cameFrom, String[] currentState) {
        List<String[]> path = new ArrayList<>();
        path.add(currentState);

        while (cameFrom.containsKey(Arrays.toString(currentState))) {
            currentState = cameFrom.get(Arrays.toString(currentState));
            path.add(currentState);
        }

        Collections.reverse(path);
        return path;
    }

    // Obtener los estados vecinos (movimientos válidos)
    private List<String[]> obtenerVecinos(String[] state) {
        List<String[]> neighbors = new ArrayList<>();
        int emptyIndex = Arrays.asList(state).indexOf("X");
        int row = emptyIndex / 3;
        int col = emptyIndex % 3;

        // Define valid moves (up, down, left, right)
        int[][] moves = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] move : moves) {
            int newRow = row + move[0];
            int newCol = col + move[1];
            if (newRow >= 0 && newRow < 3 && newCol >= 0 && newCol < 3) {
                int newIndex = newRow * 3 + newCol;
                String[] newState = state.clone();
                newState[emptyIndex] = newState[newIndex];
                newState[newIndex] = "X";
                neighbors.add(newState);
            }
        }
        return neighbors;
    }

    private void ejecutarMovimiento(String[] state) {
        requireActivity().runOnUiThread(() -> {
            for (int i = 0; i < state.length; i++) {
                TextView piece = matriz[i / 3][i % 3];
                String texto = state[i];
                String textoActual = piece.getText().toString();

                if (!texto.equals(textoActual)) {
                    // Encontrar la posición de la pieza a intercambiar
                    int[] pos = obtenerPosicionPorTexto(texto);
                    if (pos != null) {
                        intercambiarPiezas(pos[0], pos[1], i / 3, i % 3);
                    }
                }
            }
            actualizarEstadoActual();
            if (verificarEstadoCorrecto()) {
                Toast.makeText(requireContext(), "¡Resuelto!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Obtener posición de una pieza por su texto
    private int[] obtenerPosicionPorTexto(String texto) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (matriz[i][j].getText().toString().equals(texto)) {
                    return new int[]{i, j};
                }
            }
        }
        return null;
    }

    // Función para verificar si el rompecabezas está en el estado correcto
    private boolean verificarEstadoCorrecto() {
        for (int i = 0; i < currentState.length; i++) {
            if (!currentState[i].equals(goalState[i])) {
                return false;
            }
        }

        stopTimer();
        mostrarDialogoExito();
        return true;
    }

    private void mostrarDialogoExito() {
        // Crear estilo personalizado para el texto
        SpannableStringBuilder mensaje = new SpannableStringBuilder();
        mensaje.append("¡Felicidades!\n", new StyleSpan(Typeface.BOLD), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mensaje.append("Has completado el rompecabezas en " + tvContador.getText().toString() + " Pasos!", new RelativeSizeSpan(0.9f), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Crear diálogo con Material Design
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
                .setTitle("¡Éxito! �")
                .setMessage(mensaje)
                .setPositiveButton("Genial", (dialogInterface, which) -> {
                    // Acción al hacer clic
                    // Cerrar el fragmento actual y regresar al MainActivity
                    FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                    fragmentManager.popBackStack(); // Elimina el fragmento de la pila de retroceso
                })
                .setCancelable(false);

        // Mostrar el diálogo
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        // Cambiar el color del botón "Genial" a verde con texto blanco
        Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (positiveButton != null) {
            positiveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white)); // Texto blanco
            positiveButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.green_success))); // Fondo verde
        }

        // Opcional: Agregar animación
        vibrarDispositivo();
    }

    // Vibrar al completar (opcional)
    private void vibrarDispositivo() {
        Vibrator vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
            }
        }
    }

    // Función para actualizar el estado actual del rompecabezas
    private void actualizarEstadoActual() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                currentState[i * 3 + j] = matriz[i][j].getText().toString();
            }
        }
    }

    // Mover pieza manualmente
    private void moverPieza(TextView pieza) {
        int[] posicionPieza = obtenerPosicion(pieza);
        int fila = posicionPieza[0];
        int columna = posicionPieza[1];

        // Variable para verificar si se realizó un movimiento válido
        boolean movimientoValido = false;

        // Verificar si la pieza adyacente es la casilla vacía ("X")
        if (fila > 0 && matriz[fila - 1][columna].getText().toString().equals("X")) {
            intercambiarPiezas(fila, columna, fila - 1, columna);
            movimientoValido = true;
        } else if (fila < 2 && matriz[fila + 1][columna].getText().toString().equals("X")) {
            intercambiarPiezas(fila, columna, fila + 1, columna);
            movimientoValido = true;
        } else if (columna > 0 && matriz[fila][columna - 1].getText().toString().equals("X")) {
            intercambiarPiezas(fila, columna, fila, columna - 1);
            movimientoValido = true;
        } else if (columna < 2 && matriz[fila][columna + 1].getText().toString().equals("X")) {
            intercambiarPiezas(fila, columna, fila, columna + 1);
            movimientoValido = true;
        }

        // Solo incrementar el contador si se realizó un movimiento válido
        if (movimientoValido) {
            // Guardar el movimiento manual
            movimientosManuales.add(currentState.clone());

            // Actualizar el estado actual después del movimiento
            actualizarEstadoActual();

            // Verificar si el rompecabezas está resuelto
            if (verificarEstadoCorrecto()) {
                Toast.makeText(requireContext(), "¡Rompecabezas resuelto!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Intercambiar dos piezas
    private void intercambiarPiezas(int fila1, int columna1, int fila2, int columna2) {
        // Obtener referencias a los TextView
        TextView pieza1 = matriz[fila1][columna1];
        TextView pieza2 = matriz[fila2][columna2];

        // Intercambiar texto
        String tempText = pieza1.getText().toString();
        pieza1.setText(pieza2.getText());
        pieza2.setText(tempText);

        // Intercambiar fondo
        Drawable tempBackground = pieza1.getBackground();
        pieza1.setBackground(pieza2.getBackground());
        pieza2.setBackground(tempBackground);

        // Intercambiar color del texto
        int tempColor = pieza1.getCurrentTextColor();
        pieza1.setTextColor(pieza2.getCurrentTextColor());
        pieza2.setTextColor(tempColor);
        conteo++;
        tvContador.setText(String.valueOf(conteo));
    }

    // Obtener la posición de una pieza en la matriz
    private int[] obtenerPosicion(TextView pieza) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (matriz[i][j] == pieza) {
                    return new int[]{i, j};
                }
            }
        }
        return new int[]{-1, -1};
    }

    // Clase que representa un estado y su costo en la búsqueda A*
    static class State {
        String[] state;
        int f;  // f = g + h
        int g;  // Costo desde el inicio
        int h;  // Heurística

        State(String[] state, int f, int h) {
            this.state = state;
            this.f = f;
            this.h = h;
            this.g = f - h;
        }

        int getF() {
            return f;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            State state1 = (State) obj;
            return Arrays.equals(state, state1.state);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(state);
        }
    }
}