import jdk.jshell.spi.ExecutionControl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.*;

public class MicroBlog implements SocialNetwork{

    private Map<String, Set<String>> utentiSeguiti;//associa ad ogni username la lista di utenti seguiti; identifica anche
    // con il suo keySet, l'insieme degli utenti iscritti alla rete
    private Map<String, Set<Post>> postUtente;// associa ad ogni username la lista di post scritti; se l'utente
    // non ha mai postato non sarà presente come chiave
    private Map<Integer,Post> postGlobali;//associa ad ogni id univoco il post relativo a quell'id; struttura non necessaria
    //ma fondamentale per implementare più velocemente le operazioni di ricerca di un post pubblicato nella rete

    //Overview: MicroBlog è un tipo di dato modificabile che permette di gestire la registrazione di utenti, la pubblicazione di post
    //da parte di utenti registrati e la possibilità di mettere like e seguire altri utenti

    //Typical Element: Un elemento tipico è l'associazione di ogni utente_U registrato con gli utenti che segue e 
    // con i post che egli ha scritto. Tutti gli utenti che l'utente_U segue sono tutti e soli quelli a cui ha messo like a un post della rete
    //Esempio: f:{lista di utenti}->{{utenti seguiti}, {post_scritti}}

    //AF(c): f(utente_u)={utentiSeguiti.get(utente_u), postUtente.get(utente_u)}
    // dove il dominio della funzione è l'insieme degli utenti iscritti, il codominio è una coppia di insiemi 
    // (sottoinsieme degli utenti iscritti e un insieme dei post pubblicati dall'utente) e dove
    // utente_u è registrato <=> utentiSeguiti.containsKey(utente_u) e dove il concetto di utente che segue un altro utente è intrinseco all'interno di un post,
    //ovvero utente_u segue utente_v <=> ∃ post in postUtente.get(utente) && post.getLikes.contain(user_u)
    //La funzione di astrazione, così come la Map utentiSeguiti, è determinata univocamente dalla Map postUtente: è questa Map
    //che definisce lo stato concreto del mio dato MicroBlog

    //IR(c): utentiSeguiti!=null && postGlobali!=null && postUtente!=null &&
    //       ---for all user in utentiSeguiti.keySet() => (!user.isEmpty() && utentiSeguiti.get(user)!=null && for all user1 in utentiSeguiti.get(user) => utentiSeguiti.containsKey(user1))
    //       *posso seguire soltanto un utente registrato, il codominio della funzione è un insieme di utenti registrati*
    //       &&
    //       ---for all user in postUtente.keySet() => (postUtente.get(user)!=null && utentiSeguiti.containsKey(user) *solo gli utenti registrati possono
    //       scrivere un post * && for all post in postUtente.get(user)=> (post != null && post.author.equals(user) && postGlobali.containsValue(post) *tutti
    //       i post associati ad un utente hanno come autore quell'utente e tutti i post scritti da un utente iscritto sono tra i post globali*
    //       && for all likes in post.getLikes() => ( utentiSeguiti.containKey(likes) * I likes ai post sono tutti di utenti registrati perché definiscono la semantica del “seguire”*
    //       && utentiSeguiti.get(like).contains(post.author))) *tutti gli utenti che hanno messo like ad un post seguiranno l'autore del post*
    //       &&
    //       ---for all post in postGlobali.values() => (post!=null && utentiSeguiti.containsKey(post.author) *solo gli utenti registrati possono
    //       scrivere un post * &&
    //       postUtente.containsKey(post.author) && postUtente.get(post.author).contains(post)) *un post globale
    //       è sempre associato ad un utente nella struttura postUtente*
    //       &&
    //       ---for all id in postGlobali.keySet() => (postGlobali.get(id).getId == id)

    public MicroBlog() {
        utentiSeguiti = new HashMap<>();
        postUtente = new HashMap<>();
        postGlobali= new HashMap<>();
    }

    //requires: username !=null && !username.isEmpty()
    //throws: NullPointerException se username==null
    //        SocialException se utentiSeguiti.containsKey(username) || username.isEmpty("")
    //modifies: this
    //effects: se l'utente non è ancora registrato, viene registrato come utente che non ha alcun post
    //e non segue nessuno
    public void registraUtente(String username) throws NullPointerException, SocialException {
        if (username == null) {
            throw new NullPointerException();
        }
        if (utentiSeguiti.containsKey(username))//controlliamo se l'utente è già iscritto
            throw new SocialException("Utente già registrato/Username occupato");
        if(username.isEmpty())//controlliamo se l'username è valido
            throw new SocialException("Username non valido");
        utentiSeguiti.put(username, new HashSet<String>());
    }

    //requires: ps!=null && l'autore del post deve essere iscritto
    //throws: NullPoiterException se ps==null
    //        SocialException se si cerca di inserire un post con autore non registrato
    //modifies: this
    //effects: se l'autore del post è registrato carico il post sulla piattaforma tenendo i like solo
    //degli utenti registrati (che seguiranno l'autore del post) sulla piattaforma altrimenti lancio un'eccezione
    //che mi dice che l'utente non è registrato
    public void loadpost(Post ps) throws NullPointerException, SocialException {// meccanismo per caricare un post istanziato dall'esterno
        if (ps == null) throw new NullPointerException();
        if (!utentiSeguiti.containsKey(ps.getAuthor())) //controlliamo se l'autore del post è iscritto alla rete
            throw new SocialException("Puoi caricare solo post di utenti registrati");
        Post nuovo;
        HashSet<String> likeNuovo = new HashSet<>();
        for (String like : ps.getLikes()) {//filtriamo i like dei soli utenti iscritti: nel set likeNuovo avrò i like
            // al post dei soli utenti iscritti
            if (utentiSeguiti.containsKey(like)) { //l'utente che ha messo like è registrato
                likeNuovo.add(like);
                utentiSeguiti.get(like).add(ps.getAuthor()); //questo utente seguirà l'autore del post nella map
            }
        }
        try {
            nuovo = new Post(ps.getAuthor(), ps.getText());//istanziamo un nuovo post con relativi controlli correttezza dei parametri:
                                                            //in tal senso non ci sarà alcuna relazione tra il post esterno e quello pubblicato:
                                                            //non esponiamo rappresentazione
            nuovo.addLike(likeNuovo); // inseriamo i like dei soli utenti iscritti del post originario
            postGlobali.put(nuovo.getId(), nuovo);//inseriamo il post come post globale

            if(postUtente.containsKey(ps.getAuthor())) {//autore del post aveva scritto già altri post, quindi è presente
                postUtente.get(ps.getAuthor()).add(nuovo);//come chiave nella Map postUtente
            }
            else{
                HashSet<Post> postScritti= new HashSet<>();//primo post scritto da autore del post: va aggiunta la corrispondenza
                postScritti.add(nuovo);                     //nella map postUtente tra l'utente ed un set contenente il post appena pubblicato
                postUtente.put(ps.getAuthor(), postScritti);
            }
        }catch (PostException e){//sono sicuro che non avrò eccezioni poichè il precedente post era un istanza di Post e quindi
            // rispettava IR di Post
            System.out.println(e.toString());
        }
    }

    //requires: text!=null && author!=null e l'autore del post deve essere iscritto
    //throws: NullPoiterException se text==null || author == null
    //        SocialException se si cerca di inserire un post con autore non registrato
    //modifies: this
    //effects: se l'autore del post è registrato e il post soddisfa IR di post, carico il post
    // sulla piattaforma. Se l'autore non è registrato lancio un SocialException che mi avverte che il non può pubblicare
    //in quanto non registrato; se il post non valido lancio un'eccezione che mi avverte dell'errore e non lo carico

    public void createPost(String author, String text) throws NullPointerException, SocialException{//meccanismo per caricare un post da zero
        if(author == null || text == null){
            throw new NullPointerException();
        }
        if(!utentiSeguiti.containsKey(author)){//controllo se l'utente che vuole pubblicare è iscritto
            throw new SocialException("L'utente "+ author+ " deve essere registrato per postare");
        }
        try {
            Post ps = new Post(author, text);
            postGlobali.put(ps.getId(),ps);

            if(postUtente.containsKey(ps.getAuthor())) {//autore del post aveva scritto già altri post, quindi è presente
                postUtente.get(ps.getAuthor()).add(ps); //come chiave nella Map postUtente
            }
            else{
                HashSet<Post> postScritti= new HashSet<>();//primo post scritto da autore del post: va aggiunta la corrispondenza
                postScritti.add(ps);                        //nella map postUtente tra l'utente ed un set contenente il post appena pubblicato
                postUtente.put(ps.getAuthor(), postScritti);
            }
        }catch (PostException e){
            System.out.println(e.toString());
        }
    }
    //requires: username!=null && l'id deve riferirsi ad un post presente in rete && IR(post)
    //throws: NullPointerException se username==null, SocialException se username non è registrato o se il post non è presente in rete
    //effects: aggiunge il like al post identificato da id se questo è presente nella reta sociale, l'utente è registrato e non sta
    // mettendo like ad un suo post.
    //Lancia le eccezioni sopra citate in caso contrario
    public void likeIt(String username, int idPost) throws NullPointerException, SocialException{

        if (username == null) {
            throw new NullPointerException();
        }
        if (!postGlobali.containsKey(idPost)) { //controllo se il post è presente nella rete
            throw new SocialException("Post con id " + idPost + " non esistente nella rete");
        }
        if (!utentiSeguiti.containsKey(username)) { //controllo se il like proviane da un utente registrato
            throw new SocialException("Utente: " + username + ". Un utente non registrato non può mettere like");
        }
        try{
            Post ps = postGlobali.get(idPost);
            ps.addLike(username);//aggiungiamo il likes al post
            utentiSeguiti.get(username).add(ps.getAuthor());//username ora segue l'autore del post nella Map utentiSeguiti
        }catch (PostException e){
            System.out.println(e.toString());
        }
    }
    //requires: il post è presente nella rete
    //throws: SocialException se !postGlobali.containKey(id)
    //effecs: rimuove il post dalla rete sociale (se presente) e di conseguenza tutte le relazioni che scaturiscono da esso: rimuovo
    //l'autore del post dagli utenti seguiti da ciascun utente_u che ha messo like al post <=> il like al post messo dall'utente_u era
    //l'unico like messo fra tutti i post pubblicati dall'autore del post. Applico una removeLike a tutti i like del post per eliminare relazioni scaturite dal post
    public void removePost(int id) throws SocialException {
        if(!postGlobali.containsKey(id)) //controllo se il post è presente nella rete
            throw new SocialException("Il post non è presente nella rete");
        Post check=postGlobali.get(id);
        String authorToCheck = check.getAuthor();
        for(String utente_u : check.getLikes()){//rimuovo tutti i like ai post e le relazioni che ne derivano
            removeLike(utente_u,check.getId());
        }
        postUtente.get(authorToCheck).remove(check);//rimuovo il post fra quelli scritti dall'utente
        postGlobali.remove(id,check);//rimuovo il post fra i post globali
    }
    //requires: il post è presente nella rete e l'utente username aveva messo il like al post
    //throws:   NullPointerException se Username!=null
    //          SocialException se !postGlobali.containKey(id) && !postGlobali.get(id).contain(username)
    //effecs: rimuove il like di username dal post contrassegnato con id e di conseguenza tutte le relazioni che scaturiscono da esso: rimuovo
    //l'autore del post dagli utenti seguiti da utente username che ha messo like al post <=> il like al post messo dall'utente username era
    //l'unico like messo fra tutti i post pubblicati dall'autore del post
    public void removeLike(String username, int id) throws NullPointerException, SocialException {
        if(username==null)
            throw new NullPointerException();// non controllo se utente è iscritto perchè solo utenti iscritti possono mettere like ad un post
        if (!postGlobali.containsKey(id)) //controllo se il post è presente nella rete
            throw new SocialException("Il post non è presente nella rete");
        Post check = postGlobali.get(id);
        String authorToCheck = check.getAuthor();
        try {
            check.removeLike(username);
            if(!this.otherLike(authorToCheck,username))             //se l'utente non ha messo altri like ad altri post di authorToCheck
                utentiSeguiti.get(username).remove(authorToCheck);//allora non seguirà più authorToCheck

        }catch(PostException e) {
            System.out.println(e.toString());
        }
    }
    //effects: metodo privato che controlla se user ha messo like a qualche post di author: non inserisco clausule requires
    //perchè il metodo è chiamato solo da removeLike che passa sempre parametri validi
    private Boolean otherLike(String author, String user){
        for(Post ps: postUtente.get(author)){//controllo se l'utente ha messo like ad altri post
            if(ps.getLikes().contains(user))
                return true;//l'utente ha messo altri like al post
        }
        return false;//l'utente non ha messo altri like al post
    }
    //effects: restituisce l'insieme degli utenti registrati alla rete, ovvero , secondo la semantica sopra descritta,
    //quelli che sono presenti come chiave in utentiSeguiti
    public Set<String> utentiRegistrati(){
        return utentiSeguiti.keySet();
    }

    //requires: ps!=null && for all post in ps -> post!=null
    //throws: NullpointerException se ps==null || (exsist post in ps && post==null)
    //effects: restituisce la rete sociale derivata dalla lista di post in base alle assunzioni fatte nell'implementazione di MicroBlog:
    //tutti i post sono scritti da utenti registrati, tutti i like ai post sono di utenti registrati, i like definiscono la
    //semantica del seguire.
    static public Map<String, Set<String>> guessFollowers(List<Post> ps) throws NullPointerException {
        if (ps == null)
            throw new NullPointerException();
        Map<String, Set<String>> rete = new HashMap<>(); //creo una nuova Map
        for (Post post : ps) {
            if(post==null)
                throw new NullPointerException();
            if(!rete.containsKey(post.getAuthor())){ //iscrivo l'autore del post inserendolo nella rete
                rete.put(post.getAuthor(), new HashSet<String>());
            }
            for (String like : post.getLikes()) {
                if (rete.containsKey(like))//tutti gli utenti che hanno messo like al post seguiranno l'autore del post
                    rete.get(like).add(post.getAuthor());
                else { //iscrivo gli username che hanno messo like al post e faccio si che seguano l'autore del post
                    Set<String> seguiti = new HashSet<>();
                    seguiti.add(post.getAuthor());
                    rete.put(like, seguiti);
                }
            }
        }
        return rete;
    }

    //requires: username!=null e username iscritto alla rete sociale
    //throws: NullPoiterException se username==null; SocialException se l'utente non è iscritto alla rete sociale
    //effects: ritorna la lista di post scritti dall'utente username. Se l'utente non è registrato lancia una SocialException;
    // se l'utente è registrato ma non ha scritto alcun post ritorna una lista vuota

    public List<Post> writtenBy(String username) throws NullPointerException, SocialException{
        if (username == null) throw new NullPointerException();
        if(!utentiSeguiti.containsKey(username)) //controllo se l'utente è registrato
            throw new SocialException("L'utente non è registrato!");
        ArrayList<Post> listaPost = new ArrayList<Post>();
        if (postUtente.containsKey(username)) {     //scandisco tutti i post scritti da utente
            for(Post ps : postUtente.get(username)){//se l'utente non ha mai scritto un post ritorno la lista vuota

                Post nps = ps.clone();//facciamo una deep copy del post per evitare di esporre la rappresentazione
                listaPost.add(nps);     //e rischiare con operazioni esterne di violare IR
            }
        }
        return listaPost;
    }

    //requires: username!=null && ps!=null && for all post in ps => post!=null
    //throws: NullPointerException() se username==null || ps == null || exist post in ps && post==null
    //effects: ritorna una lista di post scritti dall'utente username. Se l'utente non ha scritto alcun post ritorna la lista vuota
    static public List<Post> writtenBy(List<Post> ps, String username) throws NullPointerException{
        if (ps == null || username == null) throw new NullPointerException();
        ArrayList<Post> lst = new ArrayList<Post>(); //istanzion una lista di post vuota

        for (Post post : ps) { //scandisco la lista di post
            if(post==null)
                throw new NullPointerException();
            if (post.getAuthor().equals(username)) //se il post è scritto da username lo aggiungo alla mia lista
                lst.add(post);
        }
        return lst;
    }


    //requires: words!=null && for all word in words => word!=null
    //throws: NullPointerException() se words==null | exist word in words && word==null
    //effects: restituisce la lista dei post presenti nella rete sociale che includono almeno una delle parole
    //presenti nella lista delle parole argomento del metodo.
    public List<Post> containing(List<String> words) throws NullPointerException {
        if(words==null)
            throw new NullPointerException();
        ArrayList<Post> lst = new ArrayList<>();//istanzio una lista di post vuota

        for(String word: words){//per ogni parola nella lista , controllo se un post la contiene; se sì la aggiungo alla mia lista lst
            if(word==null)
                throw new NullPointerException();
            for(Post ps: postGlobali.values()){
                String text= ps.getText().toLowerCase();//per evitare che diverse formattazioni della medesima parola non vengano conteggiate,
                if(text.contains(word.toLowerCase())) //rendo tutto minuscolo
                    lst.add(ps);
            }
        }
        return lst;
    }


    //requires: utentiSequiti!=null &&
    // for all user1 in utentiSeguiti.keySet => (for all user2 in utentiSeguiti.get(user1) => user2!=null))
    //throws: NullPointerException se non vengono rispettate le requires
    //effects: restituisce la lista di utenti che hanno un numero di followers maggiore del numero di persone che seguono all'interno
    //della rete sociale passata come parametro

    static public List<String> influencers(Map<String, Set<String>> utentiSeguiti) throws NullPointerException{
        if(utentiSeguiti==null)
            throw new NullPointerException();
        List<String> lst = new ArrayList<>();
        for(String user: utentiSeguiti.keySet()){
            int c=getFollowers(utentiSeguiti,user); //ottengo il numero di followers dell'utente

            if(c>utentiSeguiti.get(user).size())//se l'utente segue meno persone di quante ne seguano lui è un influencer
                lst.add(user);//lo aggiungo alla mia lista
        }
        return lst;
    }

    //requires: utentiSequiti!=null &&
    // for all user1 in utentiSeguiti.keySet => (for all user2 in utentiSeguiti.get(user1) => user2!=null))
    //throws: NullPointerException se non vengono rispettate le requires
    //effects: metodo privato utilizzato per ottenere il numero di followers di un utente esaminando una rete sociale
    static private int getFollowers(Map<String, Set<String>> utentiSeguiti, String username) throws NullPointerException{
        int c=0;
        if(utentiSeguiti==null || username==null)
            throw new NullPointerException();
        for(String userTocheck : utentiSeguiti.keySet()){// scandisco tutti gli utenti icritti per individuare quanti seguono utente username
            if(userTocheck==null)
                throw new NullPointerException();
            if(utentiSeguiti.get(userTocheck).contains(username)) //se l'utente segue username incremento contatore
                c++;
        }
        return c;
    }

    //effects: restituisce la lista di utenti che hanno scritto almeno un post
    public Set<String> getMentionedUsers(){
        HashSet<String> utenti = new HashSet<>();
        utenti.addAll(postUtente.keySet()); // postUtente contiene una corrispondenza tra utenti e postScritti
        return utenti;                      //implementato correttamente perchè il keySet di postUtente contiene tuti e soli gli utenti che hanno scritto
                                            //almeno un post
    }

    //requires: ps!=null && for all post in ps => post!=null
    //throws: NullPointerException se ps == null || exist post in ps && post==null
    //effects: restituisce tutti gli autori della lista passata come argomento.
    static public Set<String> getMentionedUsers(List<Post> ps) throws NullPointerException{
        if(ps==null)
            throw new NullPointerException();
        HashSet<String> autori = new HashSet<>(); //istanzio una lista di stringhe vuota

        for(Post post : ps){ //esamino ciascun post nella lista per inserire l'autore nella lista autori
            if(post==null)
                throw new NullPointerException();
            autori.add(post.getAuthor()); //aggiungo alla lista di autori l'autore del post che stiamo esaminando
        }
        return autori;
    }
    //effects: Stampa le relazioni fra gli utenti
    public String stampaRelazioni(){
        return "Le relazioni di [utente]->[utente che segue] sono le seguenti:\n"+utentiSeguiti+"\n";
    }
    public String postPubblicati(){
        return "I post pubblicati nella rete sono :\n"+postGlobali.values()+"\n";
    }
}
