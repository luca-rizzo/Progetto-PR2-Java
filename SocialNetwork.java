import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SocialNetwork {
    //Overview: tipo di dato modificabile che consente di modellare un social network in cui ogni utente ha la possibilità di registrarsi
    //e solo in seguito di seguire altra gente mettendo like ad un post o scrivere un post. La nozione di utente che segue un altro utente
    //è intrinseca all'interno di un post pubblicato.
    //Typical element: una funzione parziale definita sugli utenti iscritti che associa ad ogni utente la lista di utenti iscritti
    // che segue e la lista di post che ha pubblicato : f(utente_u)->{[lista di utenti che utente_u segue],[post che utente_u ha pubblicato]}

    public static Map<String, Set<String>> guessFollowers(List<Post> ps){
        throw new IllegalCallerException();//eccezione inserita perchè imposta dalla sintassi di java
    }

    public static List<String> influencers(Map<String, Set<String>> followers){
        throw new IllegalCallerException();//eccezione inserita perchè imposta dalla sintassi di java
    }
    public static Set<String> getMentionedUsers(List<Post> ps) {
        throw new IllegalCallerException();//eccezione inserita perchè imposta dalla sintassi di java
    };

    public List<Post> writtenBy(String username) throws SocialException;

    public static List<Post> writtenBy(List<Post> ps, String username){
        throw new IllegalCallerException();//eccezione inserita perchè imposta dalla sintassi di java
    };
    public List<Post> containing(List<String> words);
}
