import java.util.*;

public class test {
    public static void main(String[] args) {
        MicroBlog twitter = new MicroBlog();
        //prova di funzionamento delle meccaniche del social

        try {//registrazione di utenti
            twitter.registraUtente("Luca");
            twitter.registraUtente("Giovanni");
            twitter.registraUtente("Franco");
            twitter.registraUtente("Christian");
            twitter.registraUtente("Cesare");
            System.out.println("Gli utenti registrati sono: \n" + twitter.utentiRegistrati()+ "\n");
        } catch (SocialException e) {
            System.out.println(e);
        }
        try {
            twitter.registraUtente("");//username non può essere stringa vuota!
        } catch (SocialException e) {
            System.out.println(e);
        }
        try{ //post validi che vengono inseriti nella rete
            twitter.createPost("Luca","Oggi mi sento bene!");
            twitter.createPost("Cesare","Sono appena andato al mare!");
            twitter.createPost("Franco","Il lavoro nobilita l'uomo");
            twitter.createPost("Cesare","Che sonno che ho!");

            twitter.likeIt("Luca",1);
            twitter.likeIt("Luca",3);
            twitter.likeIt("Franco",1);
            twitter.likeIt("Christian",0);
            System.out.println(twitter.stampaRelazioni());
        //prova di caricamento di post esterni alla rete

            Post post1= new Post("Luca","oggi auguro una buona serata a tutti");
            post1.addLike("Diana");//non sarà inserito nella rete
            post1.addLike("Christian");
            post1.addLike("Giovanni");
            post1.addLike("Tiziano");
            twitter.loadpost(post1);
            System.out.println(twitter.stampaRelazioni());
            twitter.removeLike("Luca",3);//Luca rimuove like al post di Cesare ma continua a seguirlo perchè aveva messo like ad un'altro suo post
            System.out.println(twitter.stampaRelazioni());
            twitter.removePost(1);//Adesso Luca non segue più Cesare; Adesso Franco non segue più Cesare
            System.out.println(twitter.stampaRelazioni());
            twitter.removePost(5); //Christian continua a seguire Luca perchè aveva messo like al post 0; Giovanni non lo segue più
            System.out.println(twitter.stampaRelazioni());
            System.out.println(twitter.writtenBy("Christian"));//verifica post eliminati
        }catch (SocialException | PostException e){
            System.out.println(e);
        }
        try{ //un utente non registrato non può postare
            Post post1= new Post("Matteo","Auguro una buona serata a tutti");
            twitter.loadpost(post1);
        }catch (SocialException | PostException e){
            System.out.println(e);
        }
        try{ //un utente non registrato non può mettere like
            twitter.likeIt("Bruno",3);
        }catch (SocialException e){
            System.out.println(e);
        }
        //prova getMentionedUser
        System.out.println(twitter.getMentionedUsers());
        //prova writtenBy
        try {
            List<Post> posts= (twitter.writtenBy("Luca"));// i post in questa lista avranno un id diverso da quelli restituiti
            System.out.println(twitter.writtenBy("Luca"));
        }catch (SocialException e){
            System.out.println(e);
        }
        //prova containing
        ArrayList<String> word = new ArrayList<>();
        word.add("Oggi");
        word.add("MaRe");
        System.out.println(twitter.containing(word));

        //prova Guessfollowers, Influencer, WrittenBy : metodi statici
        try{
            Post post1 = new Post("Matteo","Auguro una buona serata a tutti");
            post1.addLike("Diana");
            post1.addLike("Carlo");
            post1.addLike("Giovanna");
            Post post2 = new Post("Carlo","Domani vado a pescare");
            post2.addLike("Matteo");
            post2.addLike("Diana");
            post2.addLike("Luca");
            post2.addLike("Bruno");
            post2.addLike("Giovanna");
            Post post3 = new Post("Bruno","Vota Biden!");
            post3.addLike("Diana");
            List<Post> listaPost= new ArrayList<Post>();
            listaPost.add(post1);
            listaPost.add(post2);
            listaPost.add(post3);
            Map<String, Set<String>> social = MicroBlog.guessFollowers(listaPost);
            System.out.println("La rete sociale derivat dalla lista di post è \n" + social +"\n");
            List<String> influencer= MicroBlog.influencers(social);//sfrutto la rete creata per identificare influencer
            System.out.println("Gli influencer della rete sociale in questione sono " + influencer+"\n");
            List<Post> postUsername= MicroBlog.writtenBy(listaPost,"Matteo");//sfrutto la lista di post per verificare writtenBy
            System.out.println(postUsername);
            System.out.println(MicroBlog.getMentionedUsers(listaPost));
            String c = "ciao";

        }catch (PostException e){
            System.out.println(e);
        }
        //prova completa con MicroBlogFiltrato
        try{
            ArrayList<String> paroleVietate= new ArrayList<>();
            paroleVietate.add("covid");
            paroleVietate.add("mare");
            paroleVietate.add("calore");
            paroleVietate.add("esami");
            MicroBlogFiltrato myTwitter = new MicroBlogFiltrato(paroleVietate);
            myTwitter.registraUtente("Francesco");
            myTwitter.registraUtente("Giuseppe");
            myTwitter.registraUtente("Vincenzo");
            myTwitter.registraUtente("Andrea");//4 utenti registrati
            myTwitter.createPost("Francesco","Oggi sono andato al mare!");
            myTwitter.createPost("Giuseppe","Domani ho esami...");
            myTwitter.createPost("Giuseppe","Ciao che fate"); //post non censurato
            System.out.println(myTwitter.writtenBy("Francesco"));
            System.out.println(myTwitter.writtenBy("Giuseppe"));
            System.out.println(myTwitter.postPubblicati());// i post nella lista writtenBy avranno id diverso rispetto a qulli presenti nella rete (abbiamo fatto una deep copy)

            myTwitter.likeIt("Giuseppe",12); //Giuseppe segue Francesco
            myTwitter.likeIt("Andrea",12); //Andrea segue Francesco
            myTwitter.likeIt("Vincenzo", 14); //Vincensco segue Giuseppe

             //caricamento post esterno
            Post post= new Post("Vincenzo","Il CaLore è una forma di energia");
            post.addLike("Diana");//non sarà inserita nella rete
            post.addLike("Giuseppe");
            post.addLike("Andrea");
            post.addLike("Francesco");
            myTwitter.loadpost(post);
            System.out.println(myTwitter.writtenBy("Vincenzo"));
            System.out.println(myTwitter.stampaRelazioni());
        }catch (PostException | SocialException e){
            System.out.println(e);
        }
    }
}
