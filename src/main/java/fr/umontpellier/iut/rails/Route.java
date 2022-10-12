package fr.umontpellier.iut.rails;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Route {
    /**
     * Première extrémité
     */
    private Ville ville1;
    /**
     * Deuxième extrémité
     */
    private Ville ville2;
    /**
     * Nombre de segments
     */
    private int longueur;
    /**
     * CouleurWagon pour capturer la route (éventuellement GRIS, mais pas LOCOMOTIVE)
     */
    private CouleurWagon couleur;
    /**
     * Joueur qui a capturé la route (`null` si la route est encore à prendre)
     */
    private Joueur proprietaire;
    /**
     * Nom unique de la route. Ce nom est nécessaire pour résoudre l'ambiguïté entre les routes doubles
     * (voir la classe Plateau pour plus de clarté)
     */
    private String nom;

    public Route(Ville ville1, Ville ville2, int longueur, CouleurWagon couleur) {
        this.ville1 = ville1;
        this.ville2 = ville2;
        this.longueur = longueur;
        this.couleur = couleur;
        nom = ville1.getNom() + " - " + ville2.getNom();
        proprietaire = null;
    }

    public Ville getVille1() {
        return ville1;
    }

    public Ville getVille2() {
        return ville2;
    }

    public int getLongueur() {
        return longueur;
    }

    public void setLongueur(int longueur) {this.longueur = longueur;}

    public CouleurWagon getCouleur() {
        return couleur;
    }

    public Joueur getProprietaire() {
        return proprietaire;
    }

    public void setCouleur(CouleurWagon couleur) {this.couleur = couleur;}

    public void setProprietaire(Joueur proprietaire) {
        this.proprietaire = proprietaire;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String toLog() {
        return String.format("<span class=\"route\">%s - %s</span>", ville1.getNom(), ville2.getNom());
    }

    @Override
    public String toString() {
        return String.format("[%s - %s (%d, %s)]", ville1, ville2, longueur, couleur);
    }

    /**
     * @return un objet simple représentant les informations de la route
     */
    public Object asPOJO() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("nom", getNom());
        if (proprietaire != null) {
            data.put("proprietaire", proprietaire.getCouleur());
        }
        return data;
    }

    public boolean isDouble(){
        return nom.contains("(1)") || nom.contains("(2)");
    }

    public Route routeAssocié(Jeu jeu) {

        if (nom.contains("(1)")) {
            return jeu.getRoutes().get(jeu.getRoutes().indexOf(this) + 1);
        }
        else if (nom.contains("(2)")) {
            return jeu.getRoutes().get(jeu.getRoutes().indexOf(this) -1);
        }
        return null;
    }

    public ArrayList<String> possedeCouleur(Joueur joueur){
        ArrayList<String> couleursDeck=new ArrayList<>();
        for(CouleurWagon c : CouleurWagon.getCouleursSimples()){
            if((Collections.frequency(joueur.getCartesWagon(), c)>=longueur || Collections.frequency(joueur.getCartesWagon(), c) + Collections.frequency(joueur.getCartesWagon(), CouleurWagon.LOCOMOTIVE)>=longueur) && joueur.getCartesWagon().contains(c)){
                couleursDeck.add(c.name());
            }
        }
        if(Collections.frequency(joueur.getCartesWagon(), CouleurWagon.LOCOMOTIVE)>=longueur){
            couleursDeck.add(CouleurWagon.LOCOMOTIVE.name());
        }
        return couleursDeck;
    }


    public boolean valide(Joueur joueur){
        if(proprietaire==null){
            if(isDouble() && joueur.getJeu().getJoueurs().size()<4){
                if(routeAssocié(joueur.getJeu()).getProprietaire()!=null){
                    return false;
                }
            }
            ArrayList<String> couleursPossede=possedeCouleur(joueur);
            if(couleursPossede.contains(couleur.name()) || couleursPossede.contains(CouleurWagon.LOCOMOTIVE.name()) || (!couleursPossede.isEmpty() && couleur==CouleurWagon.GRIS)){
                return true;
            }
        }
        return false;
    }

    public boolean capturer(Joueur joueur, CouleurWagon c){
        if(couleur==CouleurWagon.GRIS && c!=CouleurWagon.LOCOMOTIVE){
            setCouleur(c);
        }
        joueur.getCartesWagon().remove(c);
        joueur.getJeu().defausserCarteWagon(c);
        joueur.getCartesWagonPosees().add(c);
        joueur.retirerWagon();
        return true;
    }

    public int comptePoints() {
        switch (longueur) {
            case 1 -> {
                return 1;
            }
            case 2 -> {
                return 2;
            }
            case 3 -> {
                return 4;
            }
            case 4 -> {
                return 7;
            }
            case 6 -> {
                return 15;
            }
            case 8 -> {
                return 21;
            }
        }
        return 0;
    }
}
