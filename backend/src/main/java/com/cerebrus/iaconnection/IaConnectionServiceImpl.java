package com.cerebrus.iaconnection;

import com.cerebrus.TipoAct;
import com.cerebrus.usuario.Maestro;
import com.cerebrus.usuario.Usuario;
import com.cerebrus.usuario.UsuarioService;

import java.io.Console;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.autoconfigure.WebMvcProperties.Apiversion.Use;
import org.springframework.context.annotation.Bean;
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

    private final String apiKey = "AIzaSyCRLnqn2g-5Ovt9an-7yu1WnD-Uq7KMcUw";
    // Usamos gemini-1.5-flash por su velocidad y gratuidad
    private final String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;

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
            +"Devuelve exclusivamente un JSON con el siguiente formato: {\"tipo\": \"TIPO_TEST\", \"titulo\": \"Título de la actividad\", \"descripcion\": \"Descripción de la actividad\", \"preguntas\": [{\"enunciado\": \"Enunciado de la pregunta\", \"opciones\": [{\"texto\": \"Texto de la opción\", \"correcta\": true/false}]}]}"
            + "Genera al menos 2 preguntas. Cada pregunta solo puede tener una opcion correcta";
            case ORDEN -> "Genera una actividad de ordenación con los siguientes elementos: " + prompt+
            "Devuelve exclusivamente un JSON con el siguiente formato: {\"tipo\": \"ORDENACION\", \"titulo\": \"Título de la actividad\", \"descripcion\": \"Descripción de la actividad\", \"valores\": [{\"texto\": \"Texto del elemento\", \"orden\": número que indica el orden correcto}]}"
            + "Genera al menos 4 elementos a ordenar. Cada valor puedeser solo una direccion a una imagen o una sola palabra";
            case CARTA -> "Genera una actividad de tipo carta sobre el siguiente tema: " + prompt
            +"Devuelve exclusivamente un JSON con el siguiente formato: {\"tipo\": \"CARTA\", \"titulo\": \"Título de la actividad\", \"descripcion\": \"Descripción de la actividad\"}";
            case CRUCIGRAMA -> "Genera una actividad de crucigrama sobre el siguiente tema: " + prompt
            +"Devuelve exclusivamente un JSON con el siguiente formato: {\"tipo\": \"CRUCIGRAMA\", \"titulo\": \"Título de la actividad\", \"descripcion\": \"Descripción de la actividad\"}";
            case ABIERTA -> "Genera una actividad de pregunta abierta sobre el siguiente tema: " + prompt
            +"Devuelve exclusivamente un JSON con el siguiente formato: {\"tipo\": \"ABIERTA\", \"titulo\": \"Título de la actividad\", \"descripcion\": \"Descripción de la actividad\"}";
            case CLASIFICACION -> "Genera una actividad de clasificación sobre el siguiente tema: " + prompt
            +"Devuelve exclusivamente un JSON con el siguiente formato: {\"tipo\": \"CLASIFICACION\", \"titulo\": \"Título de la actividad\", \"descripcion\": \"Descripción de la actividad\"}";
            case TABLERO -> "Genera una actividad de tablero sobre el siguiente tema: " + prompt
            +"Devuelve exclusivamente un JSON con el siguiente formato: {\"tipo\": \"TABLERO\", \"titulo\": \"Título de la actividad\", \"descripcion\": \"Descripción de la actividad\" , \"tamaño\": \"tamaño de la actividad\"}";
            case IMAGEN -> "Genera una actividad con imágenes sobre el siguiente tema: " + prompt
            +"Devuelve exclusivamente un JSON con el siguiente formato: {\"tipo\": \"IMAGEN\", \"titulo\": \"Título de la actividad\", \"descripcion\": \"Descripción de la actividad\", \"imagen\": \"url\"}";
            default -> throw new IllegalArgumentException("400 Bad Request");
        };
    }
    
     @Override
    public String generarActividad(TipoAct tipoActividad, String prompt) {
       
        Usuario usuario = usuarioService.findCurrentUser();
        if(!(usuario instanceof Maestro)){
            throw new IllegalArgumentException("403 Forbidden");
        }
       String promptDepurado = crearPromt(tipoActividad, prompt);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

       
        Map<String, Object> textPart = Map.of("text", promptDepurado);
        Map<String, Object> contents = Map.of("parts", List.of(textPart));
        Map<String, Object> body = Map.of("contents", List.of(contents));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
       
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
            return respuesta;
        } catch (Exception e) {
            throw new IllegalArgumentException("500 API Server Error: " + e.getMessage());
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
          
            if (!tipo.equals(tipoActividad.name())) {
            throw new IllegalArgumentException("400 Bad Request tipo equivocado: "+jsonResponse);

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
                        throw new IllegalArgumentException("400 Bad Request");
                    }
                    break;
                case ORDEN:
                    if (!jsonResponse.containsKey("titulo") || !jsonResponse.containsKey("descripcion") || !jsonResponse.containsKey("valores")) {
                        throw new IllegalArgumentException("400 Bad Request");
                    }
                    break;
                case CARTA:
                    if (!jsonResponse.containsKey("titulo") || !jsonResponse.containsKey("descripcion")) {
                        throw new IllegalArgumentException("400 Bad Request");
                    }
                    break;
                case CRUCIGRAMA:
                    if (!jsonResponse.containsKey("titulo") || !jsonResponse.containsKey("descripcion")) {
                        throw new IllegalArgumentException("400 Bad Request");
                    }
                    break;
                case ABIERTA:
                    if (!jsonResponse.containsKey("titulo") || !jsonResponse.containsKey("descripcion")) {
                        throw new IllegalArgumentException("400 Bad Request");
                    }
                    break;
                case CLASIFICACION:
                    if (!jsonResponse.containsKey("titulo") || !jsonResponse.containsKey("descripcion")) {
                        throw new IllegalArgumentException("400 Bad Request");
                    }
                    break;
                case TABLERO:
                    if (!jsonResponse.containsKey("titulo") || !jsonResponse.containsKey("descripcion") || !jsonResponse.containsKey("tamaño")) {
                        throw new IllegalArgumentException("400 Bad Request");
                    }
                    break;
                case IMAGEN:
                    if (!jsonResponse.containsKey("titulo") || !jsonResponse.containsKey("descripcion") || !jsonResponse.containsKey("imagen")) {
                        throw new IllegalArgumentException("400 Bad Request");
                    }
                    break;
                default:
                    throw new IllegalArgumentException("400 Bad Request");
            }
            
            return true;
        } catch (Exception e) {
            throw new IllegalArgumentException("400 Bad Request: " + e.getMessage());
        }
       
    }
}
