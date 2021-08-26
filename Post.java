import java.util.*;

public class Post implements Cloneable{
    static private int idPost=0;
    final private int id;
    final private String author;
    final private String text;
    final private Date timestamp;
    private Set<String> likes;
    //Overview: il tipo post è un tipo di dato modificabile rappresentato da una quitupla di attributi
    //Typical element : (id_post, author_post, text_post, timestamp_post , [lista di username che hanno messo like al post rappresentata da un set])
    //AF(c) = (id, author, text, timestamp, likes)
    //IR(c) = for all string in likes => (string!=null && !string.isEmpty() && !string.equals(author))
    //        && timestamp!=null && text!=null && !text.isEmpty() && author!=null && !author.isEmpty() &&

    //requires: author!=null, text.length()<140, !author.isEmpty()
    //throws: NullPointerException(),PostException()
    //effects: restituisce un nuovo post con gli attributi passati come argomento
    public Post(String author, String text) throws NullPointerException,PostException{
        if(author == null || text == null)
            throw new NullPointerException();
        if(text.length()>140)
            throw new PostException("Post troppo lungo! Al massimo 140 caratteri");
        if(text.isEmpty())
            throw new PostException("Non puoi postare un post vuoto");
        if(author.isEmpty())
            throw new PostException("Nome autore non valido");
        this.id = idPost++;
        this.author = author;
        this.text = text;
        this.likes=new HashSet<>();
        timestamp = new Date(System.currentTimeMillis());
    }
    //effects: restituisce una deep-copy dell'istanza likes
    public Set<String> getLikes() {// non possiamo fare un semplice return perchè in questo modo esporremmo la rappresentazione
        //che sarebbe modificable da metodi esterni a quelli definiti nella classe
        Set<String> deepcopy = new HashSet<>();
        deepcopy.addAll(likes);
        return deepcopy;
    }
    //effects: restituisce il valore dell'istanza author
    public String getAuthor() {
        return author;
    }

    //effects: restituisce il valore dell'istanza id
    public int getId() {
        return id;
    }

    //effects: restituisce il valore dell'istanza text
    public String getText() {
        return text;
    }

    //effects: Stampa i vari valori delle istanze dei post
    public String toString(){
        return "Username: "+ author+"\n id Post: "+ id +"\n Text: "+ text + "\n Data: "+ timestamp+"\n\n";
    }

    //requires: author!=null
    //throws: NullPoiterException(); PostException()
    //modifies: this
    //effects: aggiunge un like al post se author!=null e author non è una stringa vuota altrimenti lancia una NullPointerException()
    // se  author==null e una PostException se author è la stringa vuota
    public void addLike (String author) throws NullPointerException,PostException{
        if(author==null)
            throw new NullPointerException();
        if(author.isEmpty())
            throw new PostException("Non puoi usare una stringa vuota per mettere like!");
        if(author.equals(this.getAuthor())) //controllo se il like proviene dall'autore del post
            throw new PostException("Un utente non può mettere like ad un suo post!");
        likes.add(author);
    }

    //requires: authors!=null && for all author in authors => ( author!=null && !author.isEmpty())
    //throws: NullPoiterException(); PostException()
    //modifies: this
    //effects: aggiunge i like alla lista di post solo se tutti gli username della lista sono validi e la lista non contiene,
    //tra gli utenti che hanno messo like, l'autore del post
    public void addLike (Set<String> authors) throws NullPointerException, PostException{
        if(authors==null)
            throw new NullPointerException();
        for(String author : authors) { //scandisco tutti i like della lista e li aggiungo al post
            this.addLike(author);
        }
    }
    public void removeLike (String username) throws NullPointerException,PostException{
        if(author==null)
            throw new NullPointerException();
        if(author.isEmpty() || !this.likes.contains(username)) //controllo se il like era presente nel post
            throw new PostException("L'utente "+ username + "non aveva messo like al post");
        likes.remove(username); //rimuovo il like
    }
    //requires: ps != null
    //throws: NullPointerException se ps==null
    //effects: confronta due post e restituisce vero se hanno lo stesso id
    // (due post sono uguali se hanno lo stesso id visto che questo è univoco)
    public boolean equals(Post ps) throws NullPointerException{
        if(ps==null)
            throw new NullPointerException();
        return id == ps.id;
    }
    //effects: crea una deep copy di this per evitare di esporre la rappresentazione
    public Post clone(){
        try {
            Post newPost = new Post(this.author, this.text);
            newPost.addLike(this.likes);
            return newPost;
        }catch(PostException e){//questa eccezione non verrà mai lanciata perchè this è un'istanza valida di post,
            System.out.println(e);//quindi testo e username del post sono validi
            return null;
        }
    }
}
