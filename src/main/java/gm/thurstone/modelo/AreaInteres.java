package gm.thurstone.modelo;

/**
 * Las 10 áreas ocupacionales de la Escala de Intereses de Thurstone.
 * El código (CF, CB, …) es el de la prueba en papel; {@code nombre} y
 * {@code carreras} son de uso exclusivo del servidor y nunca se envían a la
 * vista de evaluación, para no revelar a qué área pertenece cada ocupación.
 *
 * NOTA: el mapeo área→carreras es una propuesta inicial y debe validarse con
 * el psicólogo responsable antes de usarse en producción.
 */
public enum AreaInteres {
    CF("Ciencias Físicas", "Ingeniería, Física, Arquitectura"),
    CB("Ciencias Biológicas", "Medicina, Biología, Veterinaria"),
    Cp("Computacional", "Ing. de Sistemas, Estadística, Actuaría"),
    C("Comercial", "Administración, Comercio, Banca"),
    E("Ejecutiva", "Administración de Empresas, Gestión Pública"),
    P("Persuasivo", "Derecho, Marketing, Comunicación"),
    L("Lingüística", "Periodismo, Letras, Traducción"),
    H("Humanitaria", "Trabajo Social, Psicología, Educación"),
    A("Artística", "Bellas Artes, Diseño, Arquitectura"),
    M("Musical", "Música, Composición, Dirección de Orquesta");

    private final String nombre;
    private final String carreras;

    AreaInteres(String nombre, String carreras) {
        this.nombre = nombre;
        this.carreras = carreras;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCarreras() {
        return carreras;
    }
}
