package com.cerebrus.iaconnection;

import com.cerebrus.comun.enumerados.TipoAct;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;
import com.cerebrus.usuario.maestro.Maestro;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;


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

    @Value("${GOOGLE_API_KEY}") 
    private String apiKey;
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
    public String crearPromt(TipoAct tipoActividad, String prompt) {
        return switch (tipoActividad) {
            case TEORIA -> "Genera una actividad teórica sobre el siguiente tema: " + prompt
            +"Devuelve exclusivamente un JSON con el siguiente formato: {\"tipo\": \"TEORIA\", \"titulo\": \"Título de la actividad\", \"descripcion\": \"Descripción de la actividad\"}";
            case TEST -> "Genera una actividad de tipo test con preguntas de opción múltiple sobre el siguiente tema: " + prompt
            +"Devuelve exclusivamente un JSON con el siguiente formato: {\"tipo\": \"TEST\", \"titulo\": \"Título de la actividad\", \"descripcion\": \"Descripción de la actividad\", \"preguntas\": [{\"enunciado\": \"Enunciado de la pregunta\", \"opciones\": [{\"texto\": \"Texto de la opción\", \"correcta\": true/false}]}]}"
            + "Genera al menos 2 preguntas. Cada pregunta solo puede tener una opcion correcta";
            case ORDEN -> "Genera una actividad de ordenación con los siguientes elementos: " + prompt+
            "Devuelve exclusivamente un JSON con el siguiente formato: {\"tipo\": \"ORDEN\", \"titulo\": \"Título de la actividad\", \"descripcion\": \"Descripción de la actividad\", \"valores\": [{\"texto\": \"Texto del elemento\", \"orden\": número que indica el orden correcto}]}"
            + "Genera al menos 4 elementos a ordenar. Cada valor puedeser solo una direccion a una imagen o una sola palabra";
            case CARTA -> "Genera una actividad de tipo carta sobre el siguiente tema: " + prompt
            +"Devuelve exclusivamente un JSON con el siguiente formato: {\"tipo\": \"CARTA\", \"titulo\": \"Título de la actividad\", \"descripcion\": \"Descripción de la actividad\", \"preguntas\": [{\"enunciado\": \"Enunciado de la pregunta\", \"respuesta\": {\"texto\": \"Texto de la opción\", \"correcta\": true}}]}"
            +"Genera al menos dos preguntas. Cada pregunta solo puede tener una respuesta, que debe ser correcta.";
           case CLASIFICACION -> "Genera una actividad de clasificación sobre el siguiente tema: " + prompt
            +"Devuelve exclusivamente un JSON con el siguiente formato: {\"tipo\": \"CLASIFICACION\", \"titulo\": \"Título de la actividad\", \"descripcion\": \"Descripción de la actividad\", \"preguntas\": [{\"enunciado\": \"Enunciado de la pregunta\", \"opciones\": [{\"texto\": \"Texto de la opción\", \"correcta\": true}]}]}"
            + "Genera al menos 2 preguntas. Cada pregunta solo puede tener opciones correctas";
            case TABLERO -> "Genera una actividad de tablero sobre el siguiente tema: " + prompt
            +"Devuelve exclusivamente un JSON con el siguiente formato: {\"tipo\": \"TABLERO\", \"titulo\": \"Título de la actividad\", \"descripcion\": \"Descripción de la actividad\" , \"tamaño\": \"TRES_X_TRES/CUATRO_X_CUATRO\", \"preguntas\": [{\"enunciado\": \"Enunciado de la pregunta\", \"respuesta\": {\"texto\": \"Texto de la opción\", \"correcta\": true}}]}"
            +"Genera 8 preguntas para tablero TRES_X_TRES o 15 para tablero CUATRO_X_CUATRO. Cada pregunta solo puede tener una respuesta, que debe ser correcta.";
           
            default -> throw new IllegalArgumentException("400 Bad Request: Tipo de actividad no soportado: " + tipoActividad);
        };
    }
    
     @Override
    public String generarActividad(TipoAct tipoActividad, String prompt) {
       System.out.println("APIkey " + apiKey);
        Usuario usuario = usuarioService.findCurrentUser();
        if(!(usuario instanceof Maestro)){
            throw new IllegalArgumentException("403 Forbidden: El usuario no es un maestro");
        }
       String promptDepurado = crearPromt(tipoActividad, prompt);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

       
        Map<String, Object> textPart = Map.of("text", promptDepurado);
        Map<String, Object> contents = Map.of("parts", List.of(textPart));
        Map<String, Object> body = Map.of("contents", List.of(contents));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
       
        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;

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
            return respuesta;
        } catch (Exception e) {
            throw new IllegalArgumentException("500 Internal Server Error: "+" Error interno de la api de geminis: " + e.getMessage());
        }
         
    }
    
    private String cleanJsonResponse(String response) {
        return response
            .replaceAll("^```(?:json)?\\n?", "")
            .replaceAll("\\n?```$", "")
            .trim();
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
}
