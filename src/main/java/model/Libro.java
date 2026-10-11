package model;

public class Libro {
    private int id;
    private String titulo;
    private String autor;
    private String isbn;
    private int idCategoria;
    private String categoria;
    private int stock;

    public Libro() {
    }

    public Libro(int id, String titulo, String autor,
                 String isbn, int idCategoria,
                 String categoria, int stock) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.isbn = isbn;
        this.idCategoria = idCategoria;
        this.categoria = categoria;
        this.stock = stock;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public int getIdCategoria() { return idCategoria; }
    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    @Override
    public String toString() {
        return titulo + " (stock: " + stock + ")";
    }
}

