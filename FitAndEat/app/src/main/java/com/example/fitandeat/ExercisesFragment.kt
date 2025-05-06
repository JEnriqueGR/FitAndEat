package com.example.fitandeat

import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner

class ExercisesFragment : Fragment() {

    private lateinit var adapter: ExerciseAdapter
    private lateinit var allEjercicios: List<Exercise>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_exercise, container, false)

        // 1. Referencias a vistas
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvEjercicios)
        val searchBar = view.findViewById<EditText>(R.id.etSearch)
        val spinner = view.findViewById<Spinner>(R.id.spMuscleFilter) // ← Asegúrate de que el id sea correcto

        // 2. Datos base
        allEjercicios = getEjerciciosDeEjemplo()
        adapter = ExerciseAdapter(requireContext(), allEjercicios)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // 3. Configurar el Spinner con los grupos musculares
        val gruposMusculares = listOf("Todos", "Pecho", "Espalda", "Hombro", "Biceps", "Triceps", "Pierna", "Abdomen")

        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, gruposMusculares)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter

        // 4. Escucha cambios del Spinner
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val grupoSeleccionado = gruposMusculares[position]
                val ejerciciosFiltrados = if (grupoSeleccionado == "Todos") {
                    allEjercicios
                } else {
                    allEjercicios.filter { it.musculo == grupoSeleccionado }
                }
                adapter = ExerciseAdapter(requireContext(), ejerciciosFiltrados)
                recyclerView.adapter = adapter
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // 5. Filtrado por texto
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val texto = s.toString().trim()
                val ejerciciosVisibles = if (spinner.selectedItem.toString() == "Todos") {
                    allEjercicios
                } else {
                    allEjercicios.filter { it.musculo == spinner.selectedItem.toString() }
                }
                val filtrados = ejerciciosVisibles.filter {
                    it.nombre.contains(texto, ignoreCase = true)
                }
                adapter = ExerciseAdapter(requireContext(), filtrados)
                recyclerView.adapter = adapter
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        return view
    }

    private fun getEjerciciosDeEjemplo(): List<Exercise> {
        return listOf(
            // --- PECHO ---
            Exercise(
                nombre = "Barbell Bench Press",
                descripcion = "Press de banca plano con barra. Ejercicio base para fuerza y masa en pecho.",
                imagenResId = R.drawable.barbell_bench_press,
                alternativas = listOf(
                    AlternativeExercise("Incline Dumbbell Press", R.drawable.incline_dumbbell_press),
                    AlternativeExercise("Decline Barbell Press", R.drawable.decline_barbell_press),
                    AlternativeExercise("Close-grip Bench Press", R.drawable.closegrip_bench)
                ),
                musculo = "Pecho"
            ),
            Exercise(
                nombre = "Incline Dumbbell Press",
                descripcion = "Press inclinado con mancuernas para enfatizar la parte superior del pecho.",
                imagenResId = R.drawable.incline_dumbbell_press,
                alternativas = listOf(
                    AlternativeExercise("Incline Smith Machine Press", R.drawable.incline_smith),
                    AlternativeExercise("Barbell Bench Press", R.drawable.barbell_bench_press)
                ),
                musculo = "Pecho"
            ),
            Exercise(
                nombre = "Converging Chest Press (Machine)",
                descripcion = "Press en máquina convergente para rango de movimiento controlado y seguro.",
                imagenResId = R.drawable.converging_chest_machine,
                alternativas = listOf(
                    AlternativeExercise("Cable Crossover", R.drawable.cable_crossover)
                ),
                musculo = "Pecho"
            ),
            Exercise(
                nombre = "Dips (Chest version)",
                descripcion = "Fondos inclinados al frente para estimular principalmente los pectorales.",
                imagenResId = R.drawable.dips_chest,
                alternativas = listOf(
                    AlternativeExercise("Push-ups", R.drawable.pushup),
                    AlternativeExercise("Close-grip Bench Press", R.drawable.closegrip_bench)
                ),
                musculo = "Pecho"
            ),
            Exercise(
                nombre = "Decline Barbell Press",
                descripcion = "Press de banca declinado para trabajar la parte inferior del pecho.",
                imagenResId = R.drawable.decline_barbell_press,
                alternativas = listOf(
                    AlternativeExercise("Barbell Bench Press", R.drawable.barbell_bench_press),
                    AlternativeExercise("Push-ups", R.drawable.pushup)
                ),
                musculo = "Pecho"
            ),
            Exercise(
                nombre = "Incline Smith Machine Press",
                descripcion = "Press inclinado en máquina Smith para mayor estabilidad y enfoque en la parte alta del pecho.",
                imagenResId = R.drawable.incline_smith,
                alternativas = listOf(
                    AlternativeExercise("Incline Dumbbell Press", R.drawable.incline_dumbbell_press)
                ),
                musculo = "Pecho"
            ),
            Exercise(
                nombre = "Close-grip Bench Press",
                descripcion = "Press de banca con agarre cerrado, enfoque en pecho interno y tríceps.",
                imagenResId = R.drawable.closegrip_bench,
                alternativas = listOf(
                    AlternativeExercise("Barbell Bench Press", R.drawable.barbell_bench_press),
                    AlternativeExercise("Dips", R.drawable.dips_chest)
                ),
                musculo = "Pecho"
            ),
            Exercise(
                nombre = "Cable Crossover",
                descripcion = "Cruce de cables para máxima contracción y definición de pectoral.",
                imagenResId = R.drawable.cable_crossover,
                alternativas = listOf(
                    AlternativeExercise("Converging Chest Press", R.drawable.converging_chest_machine)
                ),
                musculo = "Pecho"
            ),
            Exercise(
                nombre = "Push-ups",
                descripcion = "Flexiones para un estímulo progresivo en pecho.",
                imagenResId = R.drawable.pushup,
                alternativas = listOf(
                    AlternativeExercise("Dips", R.drawable.dips_chest),
                    AlternativeExercise("Decline Barbell Press", R.drawable.decline_barbell_press)
                ),
                musculo = "Pecho"
            ),
            // --- ESPALDA ---
            Exercise(
                nombre = "Deadlift",
                descripcion = "Peso muerto convencional, activa toda la cadena posterior.",
                imagenResId = R.drawable.deadlift_image,
                alternativas = listOf(
                    AlternativeExercise("Barbell Row", R.drawable.barbell_row),
                    AlternativeExercise("T-Bar Row", R.drawable.tbar_row)
                ),
                musculo = "Espalda"
            ),
            Exercise(
                nombre = "Pull-ups (pronated grip)",
                descripcion = "Dominadas con agarre pronado, gran activación de dorsales.",
                imagenResId = R.drawable.pullups_image,
                alternativas = listOf(
                    AlternativeExercise("Chin-ups", R.drawable.chinups_image),
                    AlternativeExercise("Lat Pulldown", R.drawable.latpulldown_image)
                ),
                musculo = "Espalda"
            ),
            Exercise(
                nombre = "Barbell Row",
                descripcion = "Remo con barra, desarrolla el grosor de la espalda.",
                imagenResId = R.drawable.barbell_row,
                alternativas = listOf(
                    AlternativeExercise("T-Bar Row", R.drawable.tbar_row),
                    AlternativeExercise("One-arm Dumbbell Row", R.drawable.dumbbell_row)
                ),
                musculo = "Espalda"
            ),
            Exercise(
                nombre = "Hammer Strength Row",
                descripcion = "Remo en máquina Hammer, brazos independientes y trayectoria fija.",
                imagenResId = R.drawable.hammer_strength_row,
                alternativas = listOf(
                    AlternativeExercise("Barbell Row", R.drawable.barbell_row),
                    AlternativeExercise("T-Bar Row", R.drawable.tbar_row)
                ),
                musculo = "Espalda"
            ),
            Exercise(
                nombre = "Cable Pullover",
                descripcion = "Pull-over en polea alta, enfocado en dorsales sin mucho bíceps.",
                imagenResId = R.drawable.cable_pullover,
                alternativas = listOf(
                    AlternativeExercise("Lat Pulldown", R.drawable.latpulldown_image),
                    AlternativeExercise("Pull-ups", R.drawable.pullups_image)
                ),
                musculo = "Espalda"
            ),
            Exercise(
                nombre = "Lat Pulldown (Neutral grip)",
                descripcion = "Jalón en agarre neutro, activa bien dorsales sin sobrecargar codos.",
                imagenResId = R.drawable.latpulldown_image,
                alternativas = listOf(
                    AlternativeExercise("Pull-ups", R.drawable.pullups_image),
                    AlternativeExercise("Chin-ups", R.drawable.chinups_image)
                ),
                musculo = "Espalda"
            ),
            Exercise(
                nombre = "One-arm Dumbbell Row",
                descripcion = "Remo unilateral con mancuerna, controla el movimiento.",
                imagenResId = R.drawable.dumbbell_row,
                alternativas = listOf(
                    AlternativeExercise("Barbell Row", R.drawable.barbell_row),
                    AlternativeExercise("T-Bar Row", R.drawable.tbar_row)
                ),
                musculo = "Espalda"
            ),
            Exercise(
                nombre = "Face Pull",
                descripcion = "Tirón a la cara con cuerda, fortalece deltoides posteriores y romboides.",
                imagenResId = R.drawable.face_pull,
                alternativas = listOf(
                    AlternativeExercise("One-arm Dumbbell Row", R.drawable.dumbbell_row)
                ),
                musculo = "Espalda"
            ),
            Exercise(
                nombre = "T-Bar Row",
                descripcion = "Remo en barra en T, excelente para volumen y densidad.",
                imagenResId = R.drawable.tbar_row,
                alternativas = listOf(
                    AlternativeExercise("Barbell Row", R.drawable.barbell_row),
                    AlternativeExercise("Hammer Strength Row", R.drawable.hammer_strength_row)
                ),
                musculo = "Espalda"
            ),
            Exercise(
                nombre = "Chin-ups (supinated grip)",
                descripcion = "Dominadas supinas, dorsales + bíceps.",
                imagenResId = R.drawable.chinups_image,
                alternativas = listOf(
                    AlternativeExercise("Pull-ups", R.drawable.pullups_image),
                    AlternativeExercise("Lat Pulldown", R.drawable.latpulldown_image)
                ),
                musculo = "Espalda"
            ),
            // --- ABDOMEN ---
            Exercise(
                nombre = "Ab Wheel Rollout",
                descripcion = "Rodada con rueda para máxima activación del core.",
                imagenResId = R.drawable.ab_wheel_rollout,
                alternativas = listOf(
                    AlternativeExercise("Hanging Leg Raises", R.drawable.hanging_leg_raises),
                    AlternativeExercise("Cable Crunch", R.drawable.cable_crunch)
                ),
                musculo = "Abdomen"
            ),
            Exercise(
                nombre = "Hanging Leg Raises",
                descripcion = "Elevaciones de piernas colgado para abdomen bajo.",
                imagenResId = R.drawable.hanging_leg_raises,
                alternativas = listOf(
                    AlternativeExercise("Ab Wheel Rollout", R.drawable.ab_wheel_rollout),
                    AlternativeExercise("Cable Crunch", R.drawable.cable_crunch)
                ),
                musculo = "Abdomen"
            ),
            Exercise(
                nombre = "Machine Crunch",
                descripcion = "Crunch abdominal en máquina, sobrecarga efectiva.",
                imagenResId = R.drawable.machine_crunch,
                alternativas = listOf(
                    AlternativeExercise("Cable Crunch", R.drawable.cable_crunch),
                    AlternativeExercise("Weighted Plank", R.drawable.weighted_plank)
                ),
                musculo = "Abdomen"
            ),
            Exercise(
                nombre = "Cable Crunch",
                descripcion = "Crunch en polea alta, control progresivo de tensión.",
                imagenResId = R.drawable.cable_crunch,
                alternativas = listOf(
                    AlternativeExercise("Machine Crunch", R.drawable.machine_crunch),
                    AlternativeExercise("Hanging Leg Raises", R.drawable.hanging_leg_raises)
                ),
                musculo = "Abdomen"
            ),
            Exercise(
                nombre = "Weighted Plank",
                descripcion = "Plancha frontal con peso para trabajo isométrico.",
                imagenResId = R.drawable.weighted_plank,
                alternativas = listOf(
                    AlternativeExercise("Side Plank", R.drawable.side_plank),
                ),
                musculo = "Abdomen"
            ),
            Exercise(
                nombre = "Dead Bug",
                descripcion = "Control de core y estabilidad lumbar.",
                imagenResId = R.drawable.dead_bug,
                alternativas = listOf(
                    AlternativeExercise("Side Plank", R.drawable.side_plank),
                    AlternativeExercise("Russian Twists", R.drawable.russian_twists)
                ),
                musculo = "Abdomen"
            ),
            Exercise(
                nombre = "Dragon Flag",
                descripcion = "Movimiento avanzado de fuerza abdominal total.",
                imagenResId = R.drawable.dragon_flag,
                alternativas = listOf(
                    AlternativeExercise("Hanging Leg Raises", R.drawable.hanging_leg_raises),
                    AlternativeExercise("Ab Wheel Rollout", R.drawable.ab_wheel_rollout)
                ),
                musculo = "Abdomen"
            ),
            Exercise(
                nombre = "Side Plank",
                descripcion = "Plancha lateral, trabaja oblicuos y estabilidad.",
                imagenResId = R.drawable.side_plank,
                alternativas = listOf(
                    AlternativeExercise("Weighted Plank", R.drawable.weighted_plank),
                    AlternativeExercise("Dead Bug", R.drawable.dead_bug)
                ),
                musculo = "Abdomen"
            ),
            Exercise(
                nombre = "Russian Twists",
                descripcion = "Rotaciones de torso con peso para oblicuos.",
                imagenResId = R.drawable.russian_twists,
                alternativas = listOf(
                    AlternativeExercise("Dead Bug", R.drawable.dead_bug),
                    AlternativeExercise("Side Plank", R.drawable.side_plank)
                ),
                musculo = "Abdomen"
            ),
            // --- HOMBRO ---
            Exercise(
                nombre = "Standing Overhead Barbell Press",
                descripcion = "Press militar de pie, base para deltoides anteriores.",
                imagenResId = R.drawable.standing_overhead_barbell_press,
                alternativas = listOf(
                    AlternativeExercise("Seated Dumbbell Press", R.drawable.seated_dumbbell_press),
                    AlternativeExercise("Push Press", R.drawable.push_press)
                ),
                musculo = "Hombro"
            ),
            Exercise(
                nombre = "Seated Dumbbell Press",
                descripcion = "Press con mancuernas sentado, más control de core.",
                imagenResId = R.drawable.seated_dumbbell_press,
                alternativas = listOf(
                    AlternativeExercise("Standing Overhead Barbell Press", R.drawable.standing_overhead_barbell_press),
                    AlternativeExercise("Arnold Press", R.drawable.arnold_press)
                ),
                musculo = "Hombro"
            ),
            Exercise(
                nombre = "Lateral Raise (Dumbbells)",
                descripcion = "Elevaciones laterales para deltoides medios.",
                imagenResId = R.drawable.lateral_raise_dumbbells,
                alternativas = listOf(
                    AlternativeExercise("Bent-over Reverse Fly", R.drawable.bentover_reverse_fly)
                ),
                musculo = "Hombro"
            ),
            Exercise(
                nombre = "Arnold Press",
                descripcion = "Press rotacional, trabaja cabeza anterior y media del hombro.",
                imagenResId = R.drawable.arnold_press,
                alternativas = listOf(
                    AlternativeExercise("Seated Dumbbell Press", R.drawable.seated_dumbbell_press),
                    AlternativeExercise("Standing Overhead Barbell Press", R.drawable.standing_overhead_barbell_press)
                ),
                musculo = "Hombro"
            ),
            Exercise(
                nombre = "Front Raise (Plate or Dumbbell)",
                descripcion = "Elevaciones frontales para deltoides anteriores.",
                imagenResId = R.drawable.front_raise,
                alternativas = listOf(
                    AlternativeExercise("Arnold Press", R.drawable.arnold_press)
                ),
                musculo = "Hombro"
            ),
            Exercise(
                nombre = "Upright Row",
                descripcion = "Remo vertical para hombros y trapecios.",
                imagenResId = R.drawable.upright_row,
                alternativas = listOf(
                    AlternativeExercise("Front Raise", R.drawable.front_raise)
                ),
                musculo = "Hombro"
            ),
            Exercise(
                nombre = "Push Press",
                descripcion = "Press explosivo asistido por piernas.",
                imagenResId = R.drawable.push_press,
                alternativas = listOf(
                    AlternativeExercise("Standing Overhead Barbell Press", R.drawable.standing_overhead_barbell_press)
                ),
                musculo = "Hombro"
            ),
            Exercise(
                nombre = "Bent-over Reverse Fly",
                descripcion = "Aperturas invertidas para deltoides posteriores.",
                imagenResId = R.drawable.bentover_reverse_fly,
                alternativas = listOf(
                    AlternativeExercise("Face Pull", R.drawable.face_pull),
                    AlternativeExercise("Lateral Raise", R.drawable.lateral_raise_dumbbells)
                ),
                musculo = "Hombro"
            ),
            Exercise(
                nombre = "Behind the Neck Press (Smith)",
                descripcion = "Press detrás de la cabeza en máquina Smith.",
                imagenResId = R.drawable.behind_neck_press,
                alternativas = listOf(
                    AlternativeExercise("Standing Overhead Barbell Press", R.drawable.standing_overhead_barbell_press)
                ),
                musculo = "Hombro"
            ),
            // --- BÍCEPS ---
            Exercise(
                nombre = "Barbell Curl",
                descripcion = "Curl de bíceps pesado con barra.",
                imagenResId = R.drawable.barbell_curl,
                alternativas = listOf(
                    AlternativeExercise("Incline Dumbbell Curl", R.drawable.incline_dumbbell_curl),
                    AlternativeExercise("Hammer Curl", R.drawable.hammer_curl)
                ),
                musculo = "Biceps"
            ),
            Exercise(
                nombre = "Incline Dumbbell Curl",
                descripcion = "Curl inclinado en banco, máximo estiramiento del bíceps.",
                imagenResId = R.drawable.incline_dumbbell_curl,
                alternativas = listOf(
                    AlternativeExercise("Spider Curl", R.drawable.spider_curl),
                    AlternativeExercise("Barbell Curl", R.drawable.barbell_curl)
                ),
                musculo = "Biceps"
            ),
            Exercise(
                nombre = "Preacher Curl",
                descripcion = "Curl predicador en banco o máquina, máximo aislamiento.",
                imagenResId = R.drawable.preacher_curl,
                alternativas = listOf(
                    AlternativeExercise("Barbell Curl", R.drawable.barbell_curl),
                    AlternativeExercise("Spider Curl", R.drawable.spider_curl)
                ),
                musculo = "Biceps"
            ),
            Exercise(
                nombre = "Concentration Curl",
                descripcion = "Curl concentrado a una mano, enfoque absoluto.",
                imagenResId = R.drawable.concentration_curl,
                alternativas = listOf(
                    AlternativeExercise("Spider Curl", R.drawable.spider_curl)
                ),
                musculo = "Biceps"
            ),
            Exercise(
                nombre = "Hammer Curl",
                descripcion = "Curl tipo martillo, trabaja bíceps y antebrazo.",
                imagenResId = R.drawable.hammer_curl,
                alternativas = listOf(
                    AlternativeExercise("Barbell Curl", R.drawable.barbell_curl)
                ),
                musculo = "Biceps"
            ),
            Exercise(
                nombre = "Spider Curl",
                descripcion = "Curl en banco inclinado boca abajo, pico de contracción.",
                imagenResId = R.drawable.spider_curl,
                alternativas = listOf(
                    AlternativeExercise("Incline Dumbbell Curl", R.drawable.incline_dumbbell_curl),
                    AlternativeExercise("Preacher Curl", R.drawable.preacher_curl)
                ),
                musculo = "Biceps"
            ),
            Exercise(
                nombre = "Cable Curl",
                descripcion = "Curl en polea baja para tensión continua.",
                imagenResId = R.drawable.cable_curl,
                alternativas = listOf(
                    AlternativeExercise("Barbell Curl", R.drawable.barbell_curl)
                ),
                musculo = "Biceps"
            ),
            Exercise(
                nombre = "Zottman Curl",
                descripcion = "Curl con rotación para fuerza de bíceps y antebrazo.",
                imagenResId = R.drawable.zottman_curl,
                alternativas = listOf(
                    AlternativeExercise("Hammer Curl", R.drawable.hammer_curl)
                ),
                musculo = "Biceps"
            ),
            Exercise(
                nombre = "Drag Curl",
                descripcion = "Curl arrastrado hacia el torso para minimizar hombros.",
                imagenResId = R.drawable.drag_curl,
                alternativas = listOf(
                    AlternativeExercise("Barbell Curl", R.drawable.barbell_curl)
                ),
                musculo = "Biceps"
            ),

            // --- TRÍCEPS ---
            Exercise(
                nombre = "Dips (Triceps version)",
                descripcion = "Fondos de tríceps torso erguido.",
                imagenResId = R.drawable.dips_triceps,
                alternativas = listOf(
                    AlternativeExercise("Close-grip Bench Press", R.drawable.closegrip_bench),
                    AlternativeExercise("Weighted Diamond Push-ups", R.drawable.pushup)
                ),
                musculo = "Triceps"
            ),
            Exercise(
                nombre = "Rope Triceps Pushdown",
                descripcion = "Extensión de tríceps en polea alta con cuerda.",
                imagenResId = R.drawable.rope_pushdown,
                alternativas = listOf(
                    AlternativeExercise("Overhead Rope Extension", R.drawable.overhead_rope_extension),
                    AlternativeExercise("Kickbacks", R.drawable.kickbacks)
                ),
                musculo = "Triceps"
            ),
            Exercise(
                nombre = "Overhead Dumbbell Extension",
                descripcion = "Extensión de tríceps sobre la cabeza a dos manos.",
                imagenResId = R.drawable.overhead_dumbbell_extension,
                alternativas = listOf(
                    AlternativeExercise("Overhead Rope Extension", R.drawable.overhead_rope_extension),
                    AlternativeExercise("Skull Crushers", R.drawable.skullcrusher_image)
                ),
                musculo = "Triceps"
            ),
            Exercise(
                nombre = "Overhead Rope Extension",
                descripcion = "Extensión de tríceps en polea sobre cabeza.",
                imagenResId = R.drawable.overhead_rope_extension,
                alternativas = listOf(
                    AlternativeExercise("Overhead Dumbbell Extension", R.drawable.overhead_dumbbell_extension),
                    AlternativeExercise("Rope Triceps Pushdown", R.drawable.rope_pushdown)
                ),
                musculo = "Triceps"
            ),
            Exercise(
                nombre = "Kickbacks",
                descripcion = "Extensión de tríceps a un brazo atrás.",
                imagenResId = R.drawable.kickbacks,
                alternativas = listOf(
                    AlternativeExercise("Rope Triceps Pushdown", R.drawable.rope_pushdown)
                ),
                musculo = "Triceps"
            ),
            Exercise(
                nombre = "Diamond Push-ups",
                descripcion = "Flexiones diamante enfocados en tríceps.",
                imagenResId = R.drawable.diamond_pushups,
                alternativas = listOf(
                    AlternativeExercise("Weighted Diamond Push-ups", R.drawable.pushup),
                    AlternativeExercise("Dips", R.drawable.dips_triceps)
                ),
                musculo = "Triceps"
            ),
            Exercise(
                nombre = "JM Press",
                descripcion = "Híbrido entre press cerrado y extensión de tríceps.",
                imagenResId = R.drawable.jm_press,
                alternativas = listOf(
                    AlternativeExercise("Close-grip Bench Press", R.drawable.closegrip_bench)
                ),
                musculo = "Triceps"
            ),
            Exercise(
                nombre = "Floor Press",
                descripcion = "Press de banca desde el suelo, acorta recorrido.",
                imagenResId = R.drawable.floor_press,
                alternativas = listOf(
                    AlternativeExercise("Close-grip Bench Press", R.drawable.closegrip_bench)
                ),
                musculo = "Triceps"
            ),
            // --- PIERNA ---
            Exercise(
                nombre = "High-bar Squat",
                descripcion = "Sentadilla con barra alta, dominante de cuádriceps.",
                imagenResId = R.drawable.high_bar_squat,
                alternativas = listOf(
                    AlternativeExercise("Front Squat", R.drawable.front_squat),
                    AlternativeExercise("Bulgarian Split Squat", R.drawable.bulgarian_split_squat)
                ),
                musculo = "Pierna"
            ),
            Exercise(
                nombre = "Romanian Deadlift",
                descripcion = "Peso muerto rumano, enfoque en glúteos e isquiosurales.",
                imagenResId = R.drawable.romanian_deadlift,
                alternativas = listOf(
                    AlternativeExercise("Walking Lunges", R.drawable.walking_lunges)
                ),
                musculo = "Pierna"
            ),
            Exercise(
                nombre = "Leg Press",
                descripcion = "Prensa de pierna inclinada para sobrecarga controlada.",
                imagenResId = R.drawable.leg_press,
                alternativas = listOf(
                    AlternativeExercise("Step-up", R.drawable.step_up)
                ),
                musculo = "Pierna"
            ),
            Exercise(
                nombre = "Front Squat",
                descripcion = "Sentadilla frontal, demanda fuerte de core y cuádriceps.",
                imagenResId = R.drawable.front_squat,
                alternativas = listOf(
                    AlternativeExercise("High-bar Squat", R.drawable.high_bar_squat),
                    AlternativeExercise("Bulgarian Split Squat", R.drawable.bulgarian_split_squat)
                ),
                musculo = "Pierna"
            ),
            Exercise(
                nombre = "Walking Lunges",
                descripcion = "Zancadas caminando para fuerza unilateral.",
                imagenResId = R.drawable.walking_lunges,
                alternativas = listOf(
                    AlternativeExercise("Bulgarian Split Squat", R.drawable.bulgarian_split_squat),
                    AlternativeExercise("Romanian Deadlift", R.drawable.romanian_deadlift)
                ),
                musculo = "Pierna"
            ),
            Exercise(
                nombre = "Hip Thrust",
                descripcion = "Empuje de cadera para máximo desarrollo de glúteos.",
                imagenResId = R.drawable.hip_thrust,
                alternativas = emptyList(),
                musculo = "Pierna"
            ),
            Exercise(
                nombre = "Seated Leg Curl",
                descripcion = "Curl femoral sentado, mayor rango de isquiosurales.",
                imagenResId = R.drawable.seated_leg_curl,
                alternativas = listOf(
                    AlternativeExercise("Romanian Deadlift", R.drawable.romanian_deadlift)
                ),
                musculo = "Pierna"
            ),
            Exercise(
                nombre = "Standing Calf Raise",
                descripcion = "Elevación de talones de pie, trabaja los gastrocnemios.",
                imagenResId = R.drawable.standing_calf_raise,
                alternativas = emptyList(),
                musculo = "Pierna"
            ),
            Exercise(
                nombre = "Bulgarian Split Squat",
                descripcion = "Sentadilla dividida, gran demanda de balance y fuerza unilateral.",
                imagenResId = R.drawable.bulgarian_split_squat,
                alternativas = listOf(
                    AlternativeExercise("Walking Lunges", R.drawable.walking_lunges),
                    AlternativeExercise("Front Squat", R.drawable.front_squat)
                ),
                musculo = "Pierna"
            ),
            Exercise(
                nombre = "Step-up",
                descripcion = "Subida a banco con carga, trabaja fuerza y estabilidad.",
                imagenResId = R.drawable.step_up,
                alternativas = listOf(
                    AlternativeExercise("Leg Press", R.drawable.leg_press)
                ),
                musculo = "Pierna"
            )
        )
    }
}
