import java.util.ArrayList;
import java.util.List;

public class MicroBlogFiltrato extends MicroBlog{
    //Overview: MicroBlogFiltrato è un tipo di dato modificabile che permette di gestire la registrazione di utenti, la pubblicazione di post
    //da parte di utenti registrati e la possibilità di mettere like e seguire altri utenti. Non sarà consentita la pubblicazione
    //di parole ritenute offensive: se queste saranno presenti in un post, il post sarà comunque pubblicato ma le parole in questione saranno
    //censurate da asterischi.

    //Typical Element: Un elemento tipico è l'associazione di ogni utente_U registrato con gli utenti che segue e
    // con i post che egli ha scritto. Tutti gli utenti che l'utente_U segue sono tutti e soli quelli a cui ha messo like a un post della rete
    //Esempio: f:{lista di utenti}->{{utenti seguiti}, {post_scritti}}

    //AF(c)= ereditato dal padre
    //IR(c)= IR_MicroBlog(c) && paroleOffensive!=null && for all word in paroleOffensive => (word!=null && !word.isEmpty()
    //&& for all post in postGlobali.values => for !post.getText.contains(word)) * nessun post della rete contiene parole offensive,
    // ovvero nessuna parola contenuta in paroleOffensive*
    final private List<String> paroleOffensive;
    //requires: offensive!=null && for all word in paroleOffensive => (word!=null && !word.isEmpty())
    //throws: NullPointerException se exsist word in paroleOffensive && word==null
    //        IllegalArgumentException se exsist word in paroleOffensive && word.isEmpty();
    //effects: chiama il costruttore della classe padre e controlla se la lista passata come argomento è diversa da null
    // e se tutte le stringhe componenti sono diverse da null.

    public MicroBlogFiltrato(List<String> offensive) throws NullPointerException, IllegalArgumentException{
        super();
        paroleOffensive = new ArrayList<>();
        if(offensive==null)
            throw new NullPointerException();
        for(String word : offensive){
            if(word==null)
                throw new NullPointerException();
            if(word.isEmpty())
                throw new IllegalArgumentException();
            paroleOffensive.add(word);//non assegno direttamente la lista passata come argomento perchè altrimenti
                                        // avrei un riferimento dall'esterno alla mia rappresentazione interna
        }
    }
    @Override
    //requires: eredita le stesse precondizioni del supertipo
    //throws: lancia le stesse eccenzioni del supertipo
    //effects: prima di chiamare il metodo del supertipo effettua un controllo sul testo per verificare se contiene
    //eventuali parole offensive definite nella nostra lista. Se le contiene queste verranno filtrate, sostituendole con
    //degli asterischi
    public void createPost(String author, String text) throws NullPointerException, SocialException {
        if(text==null || author== null)
            throw new NullPointerException();
        String newText = text;
        for(String word : paroleOffensive){
            newText = newText.replaceAll(word,"****");
        }
        super.createPost(author, newText);
    }

    @Override
    //requires: eredita le stesse precondizioni del supertipo && ps!=null
    //throws: lancia le stesse eccenzioni del supertipo
    //effects: prima di chiamare il metodo del supertipo effettua un controllo sul testo per verificare se contiene
    //eventuali parole offensive definite nella nostra lista. Se le contiene queste verranno filtrate, sostituendole con
    //degli asterischi. Successivamente passerò il post con il testo filtrato al metodo del supertipo.

    public void loadpost(Post ps) throws NullPointerException, SocialException {
        if(ps==null)
            throw new NullPointerException();
        String newText = ps.getText();
        for(String word: paroleOffensive){

            newText=newText.replaceAll("(?i)"+ word ," ****"); //permette di fare una sstituzione non case sensitive:
                                                                                //Casa, CaSa, casa sono interpretate come parole uguali
        }
        try {
            Post postFiltrato = new Post(ps.getAuthor(), newText); //creo lo stesso identico post con il testo filtrato
            postFiltrato.addLike(ps.getLikes());
            super.loadpost(postFiltrato); //richiamo il metodo del supertipo
        }catch (PostException e ){
            System.out.println(e);
        }
    }
}
