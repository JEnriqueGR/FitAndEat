package com.example.fitandeat

object SuggestedRoutineRepository {

    fun obtenerRutinasPorObjetivo(objetivo: String): List<Rutina> {
        return when (objetivo.lowercase()) {
            "perder grasa" -> listOf(
                Rutina(
                    nombre = "Full Body Quema Grasa",
                    ejercicios = listOf(
                        EjercicioRutina("Deadlift", listOf(SerieRutina(1, "Deadlift", 0f, 0, false))),
                        EjercicioRutina("Pull-ups (pronated grip)", listOf(SerieRutina(1, "Pull-ups (pronated grip)", 0f, 0, false))),
                        EjercicioRutina("Barbell Bench Press", listOf(SerieRutina(1, "Barbell Bench Press", 0f, 0, false))),
                        EjercicioRutina("High-bar Squat", listOf(SerieRutina(1, "High-bar Squat", 0f, 0, false))),
                        EjercicioRutina("Cable Crunch", listOf(SerieRutina(1, "Cable Crunch", 0f, 0, false)))
                    )
                ),
                Rutina(
                    nombre = "Circuito HIIT con Peso",
                    ejercicios = listOf(
                        EjercicioRutina("Push-ups", listOf(SerieRutina(1, "Push-ups", 0f, 0, false))),
                        EjercicioRutina("Walking Lunges", listOf(SerieRutina(1, "Walking Lunges", 0f, 0, false))),
                        EjercicioRutina("Russian Twists", listOf(SerieRutina(1, "Russian Twists", 0f, 0, false))),
                        EjercicioRutina("Pull-ups (pronated grip)", listOf(SerieRutina(1, "Pull-ups (pronated grip)", 0f, 0, false)))
                    )
                )
            )
            "ganar músculo" -> listOf(
                Rutina(
                    nombre = "Rutina Hipertrofia Pecho-Espalda",
                    ejercicios = listOf(
                        EjercicioRutina("Incline Dumbbell Press", listOf(SerieRutina(1, "Incline Dumbbell Press", 0f, 0, false))),
                        EjercicioRutina("Barbell Row", listOf(SerieRutina(1, "Barbell Row", 0f, 0, false))),
                        EjercicioRutina("Cable Crossover", listOf(SerieRutina(1, "Cable Crossover", 0f, 0, false))),
                        EjercicioRutina("Lat Pulldown (Neutral grip)", listOf(SerieRutina(1, "Lat Pulldown (Neutral grip)", 0f, 0, false)))
                    )
                ),
                Rutina(
                    nombre = "Leg Day Volumen",
                    ejercicios = listOf(
                        EjercicioRutina("Front Squat", listOf(SerieRutina(1, "Front Squat", 0f, 0, false))),
                        EjercicioRutina("Leg Press", listOf(SerieRutina(1, "Leg Press", 0f, 0, false))),
                        EjercicioRutina("Romanian Deadlift", listOf(SerieRutina(1, "Romanian Deadlift", 0f, 0, false))),
                        EjercicioRutina("Standing Calf Raise", listOf(SerieRutina(1, "Standing Calf Raise", 0f, 0, false)))
                    )
                )
            )
            "mantenerte" -> listOf(
                Rutina(
                    nombre = "Rutina de Mantenimiento General",
                    ejercicios = listOf(
                        EjercicioRutina("Barbell Bench Press", listOf(SerieRutina(1, "Barbell Bench Press", 0f, 0, false))),
                        EjercicioRutina("Pull-ups (pronated grip)", listOf(SerieRutina(1, "Pull-ups (pronated grip)", 0f, 0, false))),
                        EjercicioRutina("High-bar Squat", listOf(SerieRutina(1, "High-bar Squat", 0f, 0, false))),
                        EjercicioRutina("Machine Crunch", listOf(SerieRutina(1, "Machine Crunch", 0f, 0, false)))
                    )
                ),
                Rutina(
                    nombre = "Fuerza Básica",
                    ejercicios = listOf(
                        EjercicioRutina("Deadlift", listOf(SerieRutina(1, "Deadlift", 0f, 0, false))),
                        EjercicioRutina("Standing Overhead Barbell Press", listOf(SerieRutina(1, "Standing Overhead Barbell Press", 0f, 0, false))),
                        EjercicioRutina("Front Squat", listOf(SerieRutina(1, "Front Squat", 0f, 0, false)))
                    )
                )
            )
            else -> emptyList()
        }
    }
}
