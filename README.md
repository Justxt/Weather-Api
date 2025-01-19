Api Flight Weather Forecasting

- Api para la previsión meteorológica de vuelos

Como usarla:

1. Con el proyecto inicializado y corriendo, procedemos a ingresar a postman e insertar:

![image](https://github.com/user-attachments/assets/344a15cb-e11e-43a4-8ac8-db240ed0cb47)

- Parametros necesarios para llamar a la API: 
Key: Content-Type
Value: application/json

![image](https://github.com/user-attachments/assets/0f0b5954-c9d2-4f5c-8d79-a0b62e3cfc8e)

2. Escribimos la solicitud POST:
Ejemplo: 
http://localhost:8080/api/flight-cancellation-risk?Content-Type=application/json

![image](https://github.com/user-attachments/assets/cfddafcd-4cc6-4470-8c46-7c4d2c7d0738)

4. Se debe ingresar valores de entrada para poder obtener los resultados requieridos:
Ejemplo:
{
  "city": "Quito",
  "date": "2025-01-20"
}

- Recuerda el formato para la fecha es: (YYYY-MM-DD)

![image](https://github.com/user-attachments/assets/3e820a5f-f0e2-4d5f-b10b-2c984dcbf7d3)

4. Hacemos la solicitud y obtendremos:

{
    "riskLevel": "Baja probabilidad de cancelación",
    "message": "El clima parece favorable, tu vuelo probablemente no será afectado.",
    "weatherDetails": {
        "windSpeed": 9.4,
        "precipitation": 0.1,
        "visibility": 80.0,
        "cloudCover": 90
    },
    "latitude": -0.2201641,
    "longitude": -78.5123274
}

![image](https://github.com/user-attachments/assets/ff1dfd23-e992-47b7-ac07-786bbbb77578)
