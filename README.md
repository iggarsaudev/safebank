# 🏦 SafeBank - Full-Stack FinTech Platform

![Angular](https://img.shields.io/badge/Angular-DD0031?style=for-the-badge&logo=angular&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![TailwindCSS](https://img.shields.io/badge/Tailwind_CSS-38B2AC?style=for-the-badge&logo=tailwind-css&logoColor=white)

SafeBank es una aplicación bancaria moderna y segura desarrollada como proyecto Full-Stack. Simula un entorno bancario real donde los usuarios pueden gestionar sus cuentas, realizar transferencias (inmediatas o programadas) y visualizar estadísticas financieras interactivas.

🔗 **[Ver Demo en Producción](https://iggarsaudev-safebank.vercel.app/)**

---

## ✨ Características Principales

* **🔒 Seguridad Robusta:** Autenticación basada en **JWT (JSON Web Tokens)** y contraseñas encriptadas con BCrypt.
* **🛡️ Doble Factor (OTP):** Las transferencias superiores a 1.000€ requieren validación adicional. 
  * *Nota de despliegue:* Debido a las restricciones de puertos SMTP en el tier gratuito de Render, el envío real de emails está simulado en los logs. Para evaluar la plataforma en Producción, se ha habilitado el código maestro de bypass `123456`.
* **📊 Dashboard Interactivo:** Visualización de ingresos y gastos en tiempo real mediante un gráfico de anillo renderizado con **Chart.js**.
* **💸 Transferencias Avanzadas:** * Envíos inmediatos entre cuentas.
  * Transferencias programadas (mensuales) ejecutadas automáticamente mediante **Spring Boot @Scheduled (Cron Jobs)**.
* **📄 Generación de PDFs:** Descarga instantánea de recibos y justificantes de transferencia en formato PDF.
* **📱 Diseño Responsive:** Interfaz moderna y adaptable a dispositivos móviles creada con **Tailwind CSS**.

---

## 🛠️ Stack Tecnológico

### Frontend
* **Framework:** Angular 17/18 (Standalone Components, Signals).
* **Estilos:** Tailwind CSS.
* **Gráficos:** Chart.js.
* **Despliegue:** Vercel.

### Backend
* **Framework:** Java 21 + Spring Boot 3.
* **Seguridad:** Spring Security + JWT.
* **Base de Datos:** PostgreSQL (alojada en Neon.tech).
* **ORM:** Spring Data JPA + Hibernate.
* **Mailing:** JavaMailSender (Integración con SMTP).
* **Despliegue:** Render (Dockerizado).

---

## 🚀 Despliegue (Producción)

La arquitectura de producción está distribuida para garantizar el máximo rendimiento:
1. **Frontend:** Servido globalmente a través del CDN de **Vercel** (`/safebank-frontend`).
2. **Backend:** Web Service en **Render** operando a través de un contenedor Docker (`/safebank-backend`).
3. **Base de Datos:** Clúster de PostgreSQL Serverless en **Neon.tech**.

---

## 💻 Instalación Local

Si deseas ejecutar este proyecto en tu propia máquina:

### Requisitos Previos
* Node.js v18+ y Angular CLI.
* Java JDK 21+ y Maven.
* Base de datos PostgreSQL local o remota.

### 1. Clonar el repositorio
```bash
git clone [https://github.com/iggarsaudev/safebank.git](https://github.com/iggarsaudev/safebank.git)
cd safebank
```

### 2. Configurar el Backend
```bash
cd safebank-backend
# Configura tus variables de entorno en application.yml (DB_URL, MAIL_USERNAME, etc.)
mvn clean spring-boot:run
```

### 3. Configurar el Frontend
```bash
cd ../safebank-frontend
npm install
npm start
```
