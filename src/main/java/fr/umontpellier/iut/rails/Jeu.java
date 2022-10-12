package fr.umontpellier.iut.rails;

import com.google.gson.Gson;
import fr.umontpellier.iut.gui.GameServer;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import static fr.umontpellier.iut.rails.CouleurWagon.getCouleursSimples;

public class Jeu implements Runnable {
    /**
     * Liste des joueurs
     */
    private List<Joueur> joueurs;

    /**
     * Le joueur dont c'est le tour
     */
    private Joueur joueurCourant;
    /**
     * Liste des villes représentées sur le plateau de jeu
     */
    private List<Ville> villes;
    /**
     * Liste des routes du plateau de jeu
     */
    private List<Route> routes;
    /**
     * Pile de pioche (face cachée)
     */
    private List<CouleurWagon> pileCartesWagon;
    /**
     * Cartes de la pioche face visible (normalement il y a 5 cartes face visible)
     */
    private List<CouleurWagon> cartesWagonVisibles;
    /**
     * Pile de cartes qui ont été défaussée au cours de la partie
     */
    private List<CouleurWagon> defausseCartesWagon;
    /**
     * Pile des cartes "Destination" (uniquement les destinations "courtes", les
     * destinations "longues" sont distribuées au début de la partie et ne peuvent
     * plus être piochées après)
     */
    private List<Destination> pileDestinations;
    /**
     * File d'attente des instructions recues par le serveur
     */
    private BlockingQueue<String> inputQueue;
    /**
     * Messages d'information du jeu
     */
    private List<String> log;



    public Jeu(String[] nomJoueurs) {

        // initialisation des entrées/sorties
        inputQueue = new LinkedBlockingQueue<>();
        log = new ArrayList<>();

        // création des cartes
        pileCartesWagon = new ArrayList<>();
        cartesWagonVisibles = new ArrayList<>();
        defausseCartesWagon = new ArrayList<>();
        pileDestinations = new ArrayList<>();

        // création des joueurs
        ArrayList<Joueur.Couleur> couleurs = new ArrayList<>(Arrays.asList(Joueur.Couleur.values()));
        Collections.shuffle(couleurs);
        joueurs = new ArrayList<>();
        for (String nom : nomJoueurs) {
            Joueur joueur = new Joueur(nom, this, couleurs.remove(0));
            joueurs.add(joueur);
        }
        joueurCourant = joueurs.get(0);

        // création des villes et des routes
        Plateau plateau = Plateau.makePlateauEurope();
        villes = plateau.getVilles();
        routes = plateau.getRoutes();

        // remplissage de la pioche
        for (int i = 0; i < 12; i++) {
            pileCartesWagon.addAll(getCouleursSimples());
        }
        for(int i = 0; i < 14; i++) {
            pileCartesWagon.add(CouleurWagon.LOCOMOTIVE);
        }
        Collections.shuffle(pileCartesWagon);


        //distribuer cartes wagon
        for(int i = 0; i < nomJoueurs.length; i++) {
            for (int j = 0; j < 4; j++) {
                joueurs.get(i).getCartesWagon().add(pileCartesWagon.remove(0));
            }
        }

        //mettre cartes wagons visibles
        for(int i = 0; i < 5; i++) {
            cartesWagonVisibles.add(pileCartesWagon.remove(0));
        }

        //mélanger cartes destinations
        ArrayList<Destination> destinationscourtes = Destination.makeDestinationsEurope();
        Collections.shuffle(destinationscourtes);
        pileDestinations.addAll(destinationscourtes);
    }

    public List<CouleurWagon> getPileCartesWagon() {
        return pileCartesWagon;
    }

    public List<CouleurWagon> getCartesWagonVisibles() {
        return cartesWagonVisibles;
    }

    public List<Ville> getVilles() {
        return villes;
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public Joueur getJoueurCourant() {
        return joueurCourant;
    }

    public List<CouleurWagon> getDefausseCartesWagon() {
        return defausseCartesWagon;
    }

    public List<Destination> getPileDestinations() {
        return pileDestinations;
    }

    private ArrayList<String> gagnant(){
        ArrayList<String> g=new ArrayList<>();
        Joueur j=joueurs.get(0);
        int n=0;
        for (int i=1;i<joueurs.size();i++){
            if(joueurs.get(i).getScore()>j.getScore()){
                j=joueurs.get(i);
                n=i;
            }
        }
        for(int i=n;i<joueurs.size();i++){
            if(joueurs.get(i).getScore()==j.getScore()){
                g.add(j.getNom());
            }
        }
        return g;
    }

    /**
     * Exécute la partie
     */
    public void run() {
        ArrayList<Destination> destinationslongues = Destination.makeDestinationsLonguesEurope();
        Collections.shuffle(destinationslongues);
        for (int i=0;i<joueurs.size();i++){
            joueurCourant=joueurs.get(i);
            ArrayList<Destination> destinationsPossibles=new ArrayList<>();
            for (int j=0;j<3;j++){
                destinationsPossibles.add(piocherDestination());
            }
            destinationsPossibles.add(destinationslongues.remove(0));
            joueurCourant.choisirDestinations(destinationsPossibles,2);
        }
        int i=0;
        joueurCourant=joueurs.get(0);
        while(joueurCourant.getNbWagons()>0){
            joueurCourant.jouerTour();
            if(i>=joueurs.size()-1){
                i=0;
            }
            else{
                i++;
            }
            joueurCourant=joueurs.get(i);
        }
        prompt("Le joueur "+gagnant()+" a gagné !",new ArrayList<String>(),false);
    }

    /**
     * Ajoute une carte dans la pile de défausse.
     * Dans le cas peu probable, où il y a moins de 5 cartes wagon face visibles
     * (parce que la pioche
     * et la défausse sont vides), alors il faut immédiatement rendre cette carte
     * face visible.
     *
     * @param c carte à défausser
     */
    public void defausserCarteWagon(CouleurWagon c) {
        if (cartesWagonVisibles.size()!=5){
            cartesWagonVisibles.add(c);
        }
        else{
            defausseCartesWagon.add(c);
        }
    }

    /**
     * Pioche une carte de la pile de pioche
     * Si la pile est vide, les cartes de la défausse sont replacées dans la pioche
     * puis mélangées avant de piocher une carte
     *
     * @return la carte qui a été piochée (ou null si aucune carte disponible)
     */
    public CouleurWagon piocherCarteWagon() {
        if(pileCartesWagon.isEmpty()){
            int taille=defausseCartesWagon.size();
            for (int i=0;i<taille;i++){
                CouleurWagon carteRemplace=defausseCartesWagon.remove(0);
                pileCartesWagon.add(carteRemplace);
                Collections.shuffle(pileCartesWagon);
            }
        }
        CouleurWagon cartePioche=pileCartesWagon.remove(0);
        return cartePioche;
    }

    private void refaitPiocheVisible(){
        while(Collections.frequency(cartesWagonVisibles,CouleurWagon.LOCOMOTIVE)>=3){
            pileCartesWagon.addAll(cartesWagonVisibles);
            cartesWagonVisibles.clear();
            for(int i=0;i<5;i++){
                cartesWagonVisibles.add(piocherCarteWagon());
            }
        }
    }

    /**
     * Retire une carte wagon de la pile des cartes wagon visibles.
     * Si une carte a été retirée, la pile de cartes wagons visibles est recomplétée
     * (remise à 5, éventuellement remélangée si 3 locomotives visibles)
     */
    public void retirerCarteWagonVisible(CouleurWagon c) {
        if(cartesWagonVisibles.contains(c)){
            cartesWagonVisibles.remove(c);
            joueurCourant.getCartesWagon().add(c);
            cartesWagonVisibles.add(pileCartesWagon.remove(0));
            refaitPiocheVisible();
        }
    }

    /**
     * Pioche et renvoie la destination du dessus de la pile de destinations.
     * 
     * @return la destination qui a été piochée (ou `null` si aucune destination
     *         disponible)
     */
    public Destination piocherDestination() {
        if(pileDestinations.isEmpty()){
            return null;
        }
        else{
            Destination cartePioche=pileDestinations.remove(0);
            return cartePioche;
        }
    }

    public List<Joueur> getJoueurs() {
        return joueurs;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner("\n");
        for (Joueur j : joueurs) {
            joiner.add(j.toString());
        }
        return joiner.toString();
    }

    /**
     * Ajoute un message au log du jeu
     */
    public void log(String message) {
        log.add(message);
    }

    /**
     * Ajoute un message à la file d'entrées
     */
    public void addInput(String message) {
        inputQueue.add(message);
    }

    /**
     * Lit une ligne de l'entrée standard
     * C'est cette méthode qui doit être appelée à chaque fois qu'on veut lire
     * l'entrée clavier de l'utilisateur (par exemple dans {@code Player.choisir})
     *
     * @return une chaîne de caractères correspondant à l'entrée suivante dans la
     *         file
     */
    public String lireLigne() {
        try {
            return inputQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Envoie l'état de la partie pour affichage aux joueurs avant de faire un choix
     *
     * @param instruction l'instruction qui est donnée au joueur
     * @param boutons     labels des choix proposés s'il y en a
     * @param peutPasser  indique si le joueur peut passer sans faire de choix
     */
    public void prompt(String instruction, Collection<String> boutons, boolean peutPasser) {
        System.out.println();
        System.out.println(this);
        if (boutons.isEmpty()) {
            System.out.printf(">>> %s: %s <<<%n", joueurCourant.getNom(), instruction);
        } else {
            StringJoiner joiner = new StringJoiner(" / ");
            for (String bouton : boutons) {
                joiner.add(bouton);
            }
            System.out.printf(">>> %s: %s [%s] <<<%n", joueurCourant.getNom(), instruction, joiner);
        }

        Map<String, Object> data = Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("prompt", Map.ofEntries(
                        new AbstractMap.SimpleEntry<String, Object>("instruction", instruction),
                        new AbstractMap.SimpleEntry<String, Object>("boutons", boutons),
                        new AbstractMap.SimpleEntry<String, Object>("nomJoueurCourant", getJoueurCourant().getNom()),
                        new AbstractMap.SimpleEntry<String, Object>("peutPasser", peutPasser))),
                new AbstractMap.SimpleEntry<>("villes",
                        villes.stream().map(Ville::asPOJO).collect(Collectors.toList())),
                new AbstractMap.SimpleEntry<>("routes",
                        routes.stream().map(Route::asPOJO).collect(Collectors.toList())),
                new AbstractMap.SimpleEntry<String, Object>("joueurs",
                        joueurs.stream().map(Joueur::asPOJO).collect(Collectors.toList())),
                new AbstractMap.SimpleEntry<String, Object>("piles", Map.ofEntries(
                        new AbstractMap.SimpleEntry<String, Object>("pileCartesWagon", pileCartesWagon.size()),
                        new AbstractMap.SimpleEntry<String, Object>("pileDestinations", pileDestinations.size()),
                        new AbstractMap.SimpleEntry<String, Object>("defausseCartesWagon", defausseCartesWagon),
                        new AbstractMap.SimpleEntry<String, Object>("cartesWagonVisibles", cartesWagonVisibles))),
                new AbstractMap.SimpleEntry<String, Object>("log", log));
        GameServer.setEtatJeu(new Gson().toJson(data));
    }

}
