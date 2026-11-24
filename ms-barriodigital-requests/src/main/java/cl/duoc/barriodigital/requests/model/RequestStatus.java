package cl.duoc.barriodigital.requests.model;

public enum RequestStatus {
    INGRESADO,
    ADMITIDO,
    EN_GESTION,
    EN_TERRENO,
    RESUELTO,
    RECHAZADO;

    public static RequestStatus fromString(String text) {
        if (text == null) return null;
        String normalized = text.trim().toUpperCase().replace("Ó", "O").replace("Í", "I");
        for (RequestStatus status : RequestStatus.values()) {
            if (status.name().equals(normalized)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Estado desconocido: " + text);
    }
}
