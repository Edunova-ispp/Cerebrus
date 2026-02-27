package com.cerebrus.iaconnection;

import com.cerebrus.TipoAct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class IaConnectionServiceImpl implements IaConnectionService {

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

    @Autowired
    public IaConnectionServiceImpl() {
    }

    @Override
    public String generarMockActividad(TipoAct tipoActividad, String prompt) {
        return switch (tipoActividad) {
            case TEORIA -> JSON_TEORIA;
            case TEST -> JSON_TEST;
            case ORDEN -> JSON_ORDEN;
            default -> throw new IllegalArgumentException("Tipo de actividad no soportado: " + tipoActividad);
        };
    }
}