from setuptools import setup, find_packages

setup(
    name="watchbug",
    version="1.0.0",
    packages=find_packages(),
    package_data={
        "watchbug": ["static/*.js", "static/*.html"],
    },
    install_requires=[
        "sentry-sdk",
        "python-dotenv",
        "httpx>=0.26.0",     # Cliente HTTP ligero para reemplazar a la librería pesada
        "pydantic>=2.0.0",   # Para validación de datos
    ],
    author="rafseggom",
    description="Herramienta de reporte de bugs centralizada para usuarios pilotos",
    entry_points={
        "console_scripts": [
            "watchbug=watchbug.cli:main",
        ],
    },
)