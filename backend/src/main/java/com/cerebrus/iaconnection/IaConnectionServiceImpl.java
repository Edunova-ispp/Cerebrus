package com.cerebrus.iaconnection;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.cerebrus.comun.enumerados.TipoAct;
import com.cerebrus.exceptions.QuotaExceededException;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.maestro.Maestro;


@Service
@Transactional
public class IaConnectionServiceImpl implements IaConnectionService {
    private final RestTemplate restTemplate;
    private final UsuarioService usuarioService;
   
 
    @Autowired
    public IaConnectionServiceImpl(RestTemplate restTemplate, UsuarioService usuarioService) {
                                 
        this.restTemplate = restTemplate;
        this.usuarioService = usuarioService;
       
    }

    @Value("${GOOGLE_API_KEY_1}") 
    private String apiKey1;
    @Value("${GOOGLE_API_KEY_2}") 
    private String apiKey2;
    @Value("${GOOGLE_API_KEY_3}") 
    private String apiKey3;
    @Value("${GOOGLE_API_KEY_4}")
    private String apiKey4;
    @Value("${GOOGLE_API_KEY_5}")
    private String apiKey5;

    private Integer peticionesDiarias=0;
    private Integer IndiceKey=1;
    private LocalDate fechaUltimaPeticion;
    
    // Usamos gemini-1.5-flash por su velocidad y gratuidad
    
    private static final String JSON_TEORIA = """
            {
            "tipo": "TEORICA",
            "titulo": "Introducción a la Programación Orientada a Objetos",
            "descripcion": "En esta lección aprenderás los conceptos básicos de clases, objetos, herencia y polimorfismo."
            }
            """.strip();

    private static final String JSON_TEST = """
            {
            "tipo": "TIPO_TEST",
            "titulo": "Evaluación de Programación",
            "descripcion": "Responde a las siguientes preguntas seleccionando todas las opciones correctas.",
            "preguntas": [
            {
            "enunciado": "¿Cuáles de los siguientes son lenguajes compilados?",
            "opciones": [
            {
            "texto": "Java",
            "correcta": true
            },
            {
            "texto": "C++",
            "correcta": true
            },
            {
            "texto": "HTML",
            "correcta": false
            },
            {
            "texto": "Python",
            "correcta": false
            }
            ]
            },
            {
            "enunciado": "¿Qué conceptos pertenecen a la Programación Orientada a Objetos?",
            "opciones": [
            {
            "texto": "Encapsulación",
            "correcta": true
            },
            {
            "texto": "Herencia",
            "correcta": true
            },
            {
            "texto": "Compilación",
            "correcta": false
            },
            {
            "texto": "Polimorfismo",
            "correcta": true
            }
            ]
            }
            ]
            }
            """.strip();

    private static final String JSON_ORDEN = """
            {
            "tipo": "ORDENACION",
            "titulo": "Fases del ciclo de vida del software",
            "descripcion": "Ordena correctamente las siguientes fases.",
            "valores": [
            {
            "texto": "Análisis de requisitos",
            "orden": 1
            },
            {
            "texto": "Diseño",
            "orden": 2
            },
            {
            "texto": "Implementación",
            "orden": 3
            },
            {
            "texto": "Pruebas",
            "orden": 4
            }
            ]
            }
            """.strip();

    

    @Override
    public String generarMockActividad(TipoAct tipoActividad, String prompt) {
        return switch (tipoActividad) {
            case TEORIA -> JSON_TEORIA;
            case TEST -> JSON_TEST;
            case ORDEN -> JSON_ORDEN;
            default -> throw new IllegalArgumentException("Tipo de actividad no soportado: " + tipoActividad);
        };
    }

    public String crearPrompt(TipoAct tipoActividad, String prompt) {
        return switch (tipoActividad) {
            case TEORIA -> "Genera una actividad teórica sobre el siguiente tema: " + prompt 
            + "El objetivo de esta actividad es mostrarle información útil y breve (no más de 326 caracteres) sobre el tema solicitado a un alumno de primaria, este solo leerá la información."
            + "IMPORTANTE: No utilices ningún tipo de formato Markdown, ni guiones, ni negritas, ni listas. Devuelve solo texto plano en párrafos."
            + "Devuelve exclusivamente un JSON con el siguiente formato: {\"tipo\": \"TEORIA\", \"titulo\": \"Título de la actividad\", \"descripcion\": \"Descripción de la actividad\"}";
            case TEST -> "Genera una actividad de tipo test con preguntas de opción múltiple sobre el siguiente tema: " + prompt
            + "Devuelve exclusivamente un JSON con el siguiente formato: {\"tipo\": \"TEST\", \"titulo\": \"Título de la actividad\", \"descripcion\": \"Descripción de la actividad\", \"preguntas\": [{\"enunciado\": \"Enunciado de la pregunta\", \"opciones\": [{\"texto\": \"Texto de la opción\", \"correcta\": true/false}]}]}"
            + "Genera al menos 2 preguntas. Cada pregunta puede tener una o varias opciones correctas (no hay límite superior)."
            + "El texto de las opciones debe ser una palabra o una frase, no más"
            + "Las preguntas deben estar orientadas a alumnos de primaria y tener como consecuencia un nivel que se adapte a ello";
            case ORDEN -> "Genera una actividad de ordenación con los siguientes elementos: " + prompt
            + "Devuelve exclusivamente un JSON con el siguiente formato: {\"tipo\": \"ORDEN\", \"titulo\": \"Título de la actividad\", \"descripcion\": \"Descripción de la actividad\", \"valores\": [{\"texto\": \"Texto del elemento\", \"orden\": número que indica el orden correcto}]}"
            + "Genera al menos 4 elementos a ordenar. Cada elemento solo podrá tomar uno de los siguientes valores posibles: una dirección de imagen o bien una única palabra"
            + "Las preguntas deben estar orientadas a alumnos de primaria y tener como consecuencia un nivel que se adapte a ello";
            case CARTA -> "Genera una actividad de tipo carta sobre el siguiente tema: " + prompt
            + "Esta actividad consiste en emparejar cartas pregunta-respuesta"
            + "Devuelve exclusivamente un JSON con el siguiente formato: {\"tipo\": \"CARTA\", \"titulo\": \"Título de la actividad\", \"descripcion\": \"Descripción de la actividad\", \"preguntas\": [{\"enunciado\": \"Enunciado de la pregunta\", \"respuesta\": {\"texto\": \"Texto de la opción\", \"correcta\": true}}]}"
            + "Genera al menos dos preguntas. Cada pregunta solo puede tener una respuesta, que debe ser correcta."
            + "Las preguntas deben estar orientadas a alumnos de primaria y tener como consecuencia un nivel que se adapte a ello";
           case CLASIFICACION -> "Genera una actividad de clasificación sobre el siguiente tema: " + prompt
            + "Devuelve exclusivamente un JSON con el siguiente formato: {\"tipo\": \"CLASIFICACION\", \"titulo\": \"Título de la actividad\", \"descripcion\": \"Descripción de la actividad\", \"preguntas\": [{\"enunciado\": \"Enunciado de la pregunta\", \"opciones\": [{\"texto\": \"Texto de la opción\", \"correcta\": true}]}]}"
            + "Genera al menos 2 preguntas. Cada pregunta solo puede tener opciones correctas"
            + "Las preguntas deben estar orientadas a alumnos de primaria y tener como consecuencia un nivel que se adapte a ello";
            case TABLERO -> "Genera una actividad de tablero sobre el siguiente tema: " + prompt
            + "Esta actividad consiste en hacer preguntas al alumno para que este escriba las respuestas a ellas, para luego comparar la respuesta del alumno con la respuesta correcta preconfigurada."
            + "Se debe evitar que las preguntas sean de respuesta correcta de sí y no"
            + "Las respuestas correctas deberán ser cortas, de una palabra o una frase corta como máximo, adecuando estas al nivel de aprendizaje que estamos proporcionando"
            + "Devuelve exclusivamente un JSON con el siguiente formato: {\"tipo\": \"TABLERO\", \"titulo\": \"Título de la actividad\", \"descripcion\": \"Descripción de la actividad\" , \"tamaño\": \"TRES_X_TRES/CUATRO_X_CUATRO\", \"preguntas\": [{\"enunciado\": \"Enunciado de la pregunta\", \"respuesta\": {\"texto\": \"Texto de la opción\", \"correcta\": true}}]}"
            + "Genera 8 preguntas para tablero TRES_X_TRES o 15 para tablero CUATRO_X_CUATRO. Cada pregunta solo puede tener una respuesta, que debe ser correcta."
            + "Las preguntas y respuestas deben estar orientadas a alumnos de primaria y tener como consecuencia un nivel que se adapte a ello";
           
            default -> throw new IllegalArgumentException("400 Bad Request: Tipo de actividad no soportado: " + tipoActividad);
        };
    }
    
    @Override
    public String generarActividad(TipoAct tipoActividad, String prompt) {
        String apikeyActual;
        Integer peticionesMaximasDiariasPorKey = 20;
        Usuario usuario = usuarioService.findCurrentUser();
        if(!(usuario instanceof Maestro)){
            throw new IllegalArgumentException("403 Forbidden: El usuario no es un maestro");
        }
        String promptDepurado = crearPrompt(tipoActividad, prompt);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

       
        Map<String, Object> textPart = Map.of("text", promptDepurado);
        Map<String, Object> contents = Map.of("parts", List.of(textPart));
        Map<String, Object> body = Map.of("contents", List.of(contents));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
       
        try {
            HttpStatusCodeException last429Exception = null;

            for (int intento = 0; intento < 5; intento++) {
                if (peticionesDiarias >= peticionesMaximasDiariasPorKey) {
                    boolean rotated = rotateToNextApiKeySameDay();
                    if (!rotated) {
                        if (fechaUltimaPeticion == null || !fechaUltimaPeticion.isEqual(LocalDate.now())) {
                            fechaUltimaPeticion = LocalDate.now();
                            IndiceKey = 1;
                            peticionesDiarias = 0;
                        } else {
                            throw new QuotaExceededException("Se ha alcanzado la cuota diaria de IA. Podrás volver a intentarlo mañana.");
                        }
                    }
                }

                apikeyActual = resolveApiKeyByIndex(IndiceKey);
                System.out.println("Usando la clave de API " + IndiceKey + ": ****" + apikeyActual.substring(apikeyActual.length()-4));
                String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apikeyActual;

                try {
                    ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

                    List candidates = (List) response.getBody().get("candidates");
                    Map firstCandidate = (Map) candidates.get(0);
                    Map content = (Map) firstCandidate.get("content");
                    List parts = (List) content.get("parts");
                    Map firstPart = (Map) parts.get(0);
                    String respuesta = (String) firstPart.get("text");

                    if (!validateActivityResponse(respuesta, tipoActividad)) {
                        throw new IllegalArgumentException("respuesta no válida: " + respuesta);
                    }

                    peticionesDiarias++;
                    System.out.println("Peticiones diarias realizadas con la clave actual (" + apikeyActual.substring(apikeyActual.length()-4) + "): " + peticionesDiarias);
                    return respuesta;
                } catch (HttpStatusCodeException e) {
                    String responseBody = e.getResponseBodyAsString();
                    String errorTextRaw = (responseBody != null && !responseBody.isBlank()) ? responseBody : e.getMessage();
                    String errorText = errorTextRaw.toLowerCase();

                    if (!(e.getStatusCode().value() == 429 || errorText.contains("resource_exhausted") || errorText.contains("quota exceeded"))) {
                        throw new IllegalArgumentException("Error al generar la actividad: " + e.getMessage());
                    }

                    last429Exception = e;

                    boolean rotated = rotateToNextApiKeySameDay();
                    if (rotated) {
                        continue;
                    }

                    if (errorText.contains("perday") || errorText.contains("generaterequestsperdayperprojectpermodel-freetier")) {
                        throw new QuotaExceededException("Se ha alcanzado la cuota diaria de IA. Podrás volver a intentarlo mañana.");
                    }

                    Matcher retryMatcher = Pattern.compile("retry in\\s+([0-9]+(?:\\.[0-9]+)?)s", Pattern.CASE_INSENSITIVE)
                        .matcher(errorTextRaw);
                    if (retryMatcher.find()) {
                        double retrySeconds = Double.parseDouble(retryMatcher.group(1));
                        int retrySecondsRounded = (int) Math.ceil(retrySeconds);
                        throw new QuotaExceededException("La IA está temporalmente saturada. Reintenta en " + retrySecondsRounded + " segundos.");
                    }

                    throw new QuotaExceededException("La cuota de IA está agotada temporalmente. Inténtalo de nuevo más tarde.");
                }
            }

            if (last429Exception != null) {
                throw new QuotaExceededException("La cuota de IA está agotada temporalmente. Inténtalo de nuevo más tarde.");
            }

            throw new IllegalArgumentException("Error al generar la actividad: no se pudo completar la solicitud");
        } catch (QuotaExceededException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Error al generar la actividad: " + e.getMessage());
        }
         
    }
    
    private String cleanJsonResponse(String response) {
        return response
            .replaceAll("^```(?:json)?\\n?", "")
            .replaceAll("\\n?```$", "")
            .trim();
    }

    private String resolveApiKeyByIndex(Integer index) {
        return switch (index) {
            case 1 -> apiKey1;
            case 2 -> apiKey2;
            case 3 -> apiKey3;
            case 4 -> apiKey4;
            case 5 -> apiKey5;
            default -> throw new IllegalArgumentException("Error al seleccionar la clave de API");
        };
    }

    private boolean rotateToNextApiKeySameDay() {
        if (IndiceKey < 5) {
            IndiceKey++;
            peticionesDiarias = 0;
            return true;
        }
        return false;
    }
    
    private Boolean validateActivityResponse(String response, TipoAct tipoActividad) {
        try {
            
            String cleanedResponse = cleanJsonResponse(response);
          
            Map<String, Object> jsonResponse = new com.fasterxml.jackson.databind.ObjectMapper().readValue(cleanedResponse, Map.class);
           
            String tipo = (String) jsonResponse.get("tipo");
          
            // El prompt pide alias distintos al nombre del enum para algunos tipos
            Map<String, String> aliasToEnum = Map.of(
                "TIPO_TEST", "TEST",
                "ORDENACION", "ORDEN",
                "TEORICA", "TEORIA"
            );
            String tipoNormalizado = aliasToEnum.getOrDefault(tipo, tipo);

            if (!tipoNormalizado.equals(tipoActividad.name())) {
                throw new IllegalArgumentException("400 Bad Request tipo equivocado: " + jsonResponse);
            }
            
            
            switch (tipoActividad) {
                case TEORIA:
                    if (!jsonResponse.containsKey("titulo") || !jsonResponse.containsKey("descripcion")) {
                        System.out.println("Respuesta JSON inválida para actividad teórica: " + jsonResponse);
                        throw new IllegalArgumentException("400 Bad Request mal actividad teoria: "+jsonResponse);
                    }
                    break;
                case TEST:
                    if (!jsonResponse.containsKey("titulo") || !jsonResponse.containsKey("descripcion") || !jsonResponse.containsKey("preguntas")) {
                        throw new IllegalArgumentException("400 Bad Request mal formato de actividad test: "+jsonResponse);
                    }
                    break;
                case ORDEN:
                    if (!jsonResponse.containsKey("titulo") || !jsonResponse.containsKey("descripcion") || !jsonResponse.containsKey("valores")) {
                        throw new IllegalArgumentException("400 Bad Request mal formato de actividad orden: "+jsonResponse);
                    }
                    break;
                case CARTA:
                    if (!jsonResponse.containsKey("titulo") || !jsonResponse.containsKey("descripcion") || !jsonResponse.containsKey("preguntas")) {
                        throw new IllegalArgumentException("400 Bad Request mal formato de actividad carta: "+jsonResponse);
                    }
                    break;
                case CRUCIGRAMA:
                    if (!jsonResponse.containsKey("titulo") || !jsonResponse.containsKey("descripcion")) {
                        throw new IllegalArgumentException("400 Bad Request mal formato de actividad crucigrama: "+jsonResponse);
                    }
                    break;
                case ABIERTA:
                    if (!jsonResponse.containsKey("titulo") || !jsonResponse.containsKey("descripcion")) {
                        throw new IllegalArgumentException("400 Bad Request mal formato de actividad abierta: "+jsonResponse);
                    }
                    break;
                case CLASIFICACION:
                    if (!jsonResponse.containsKey("titulo") || !jsonResponse.containsKey("descripcion") || !jsonResponse.containsKey("preguntas")) {
                        throw new IllegalArgumentException("400 Bad Request mal formato de actividad clasificacion: "+jsonResponse);
                    }
                    break;
                case TABLERO:
                    if (!jsonResponse.containsKey("titulo") || !jsonResponse.containsKey("descripcion") || !jsonResponse.containsKey("tamaño") || !jsonResponse.containsKey("preguntas")) {
                        throw new IllegalArgumentException("400 Bad Request mal formato de actividad tablero: "+jsonResponse);
                    }
                    break;
                
                default:
                    throw new IllegalArgumentException("400 Bad Request mal tipo de actividad: "+tipoActividad);
            }
            
            return true;
        } catch (Exception e) {
            throw new IllegalArgumentException("400 Bad Request: " + e.getMessage());
        }
       
    }

    @Override
    public Map<String, Object> evaluarRespuestaAbierta(String pregunta, String respuestaAlumno, String respuestaModelo, Integer puntuacionMaxima) {
        
        String apikeyActual;
        Integer PeticionesMaximasDiariasPorKey = 20;
        
        Usuario usuario = usuarioService.findCurrentUser();
        
        String prompt = String.format(
            """
            Evalúa la siguiente respuesta de un alumno comparándola con el modelo de respuesta proporcionado por el profesor.
            
            PREGUNTA: %s
            
            RESPUESTA DEL ALUMNO: %s
            
            MODELO DE RESPUESTA DEL PROFESOR: %s
            
            Proporciona una puntuación entre 0 y %d basada en:
            1. Precisión y exactitud de la respuesta
            2. Completitud de la respuesta
            3. Consistencia con el modelo de respuesta
            
            Devuelve EXCLUSIVAMENTE un JSON con el siguiente formato:
            {
              "puntuacion": <número entre 0 y %d>,
              "comentarios": "<explicación breve de la puntuación>"
            }
            """,
            pregunta, respuestaAlumno, respuestaModelo, puntuacionMaxima, puntuacionMaxima
        );
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, Object> textPart = Map.of("text", prompt);
        Map<String, Object> contents = Map.of("parts", List.of(textPart));
        Map<String, Object> body = Map.of("contents", List.of(contents));
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        
        try {
            HttpStatusCodeException last429Exception = null;

            for (int intento = 0; intento < 5; intento++) {
                if (peticionesDiarias >= PeticionesMaximasDiariasPorKey) {
                    boolean rotated = rotateToNextApiKeySameDay();
                    if (!rotated) {
                        if (fechaUltimaPeticion == null || !fechaUltimaPeticion.isEqual(LocalDate.now())) {
                            fechaUltimaPeticion = LocalDate.now();
                            IndiceKey = 1;
                            peticionesDiarias = 0;
                        } else {
                            throw new QuotaExceededException("Se ha alcanzado la cuota diaria de IA. Podrás volver a intentarlo mañana.");
                        }
                    }
                }

                apikeyActual = resolveApiKeyByIndex(IndiceKey);
                String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apikeyActual;

                try {
                    ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

                    List candidates = (List) response.getBody().get("candidates");
                    Map firstCandidate = (Map) candidates.get(0);
                    Map content = (Map) firstCandidate.get("content");
                    List parts = (List) content.get("parts");
                    Map firstPart = (Map) parts.get(0);
                    String respuesta = (String) firstPart.get("text");

                    String cleanedResponse = cleanJsonResponse(respuesta);
                    Map<String, Object> evaluacion = new com.fasterxml.jackson.databind.ObjectMapper()
                        .readValue(cleanedResponse, Map.class);

                    peticionesDiarias++;
                    return evaluacion;
                } catch (HttpStatusCodeException e) {
                    String responseBody = e.getResponseBodyAsString();
                    String errorTextRaw = (responseBody != null && !responseBody.isBlank()) ? responseBody : e.getMessage();
                    String errorText = errorTextRaw.toLowerCase();

                    if (!(e.getStatusCode().value() == 429 || errorText.contains("resource_exhausted") || errorText.contains("quota exceeded"))) {
                        throw new IllegalArgumentException("Error al evaluar la respuesta: " + e.getMessage());
                    }

                    last429Exception = e;

                    boolean rotated = rotateToNextApiKeySameDay();
                    if (rotated) {
                        continue;
                    }

                    if (errorText.contains("perday") || errorText.contains("generaterequestsperdayperprojectpermodel-freetier")) {
                        throw new QuotaExceededException("Se ha alcanzado la cuota diaria de IA. Podrás volver a intentarlo mañana.");
                    }

                    Matcher retryMatcher = Pattern.compile("retry in\\s+([0-9]+(?:\\.[0-9]+)?)s", Pattern.CASE_INSENSITIVE)
                        .matcher(errorTextRaw);
                    if (retryMatcher.find()) {
                        double retrySeconds = Double.parseDouble(retryMatcher.group(1));
                        int retrySecondsRounded = (int) Math.ceil(retrySeconds);
                        throw new QuotaExceededException("La IA está temporalmente saturada. Reintenta en " + retrySecondsRounded + " segundos.");
                    }

                    throw new QuotaExceededException("La cuota de IA está agotada temporalmente. Inténtalo de nuevo más tarde.");
                }
            }

            if (last429Exception != null) {
                throw new QuotaExceededException("La cuota de IA está agotada temporalmente. Inténtalo de nuevo más tarde.");
            }

            throw new IllegalArgumentException("Error al evaluar la respuesta: no se pudo completar la evaluación");
        } catch (QuotaExceededException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Error al evaluar la respuesta: " + e.getMessage());
        }
    }
}
