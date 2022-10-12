package fr.umontpellier.iut.rails;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;

public class Tunnel extends Route {

    ArrayList<CouleurWagon> cartePaye;
    private boolean fullLoco;

    public Tunnel(Ville ville1, Ville ville2, int longueur, CouleurWagon couleur) {
        super(ville1, ville2, longueur, couleur);
        cartePaye = new ArrayList<>();
        fullLoco=true;
    }

    @Override
    public String toString() {
        return "[" + super.toString() + "]";
    }

    private ArrayList<CouleurWagon> cartesPiochees(Joueur joueur) {
        ArrayList<CouleurWagon> listPioche = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            CouleurWagon c = joueur.getJeu().piocherCarteWagon();
            listPioche.add(c);
            joueur.getJeu().defausserCarteWagon(c);
        }
        return listPioche;
    }

    private boolean valideApresPioche(Joueur joueur, ArrayList<CouleurWagon> carteBonus) {
        int carteAjouer = Collections.frequency(carteBonus, getCouleur()) + Collections.frequency(carteBonus, CouleurWagon.LOCOMOTIVE);
        int nbCouleur=Collections.frequency(joueur.getCartesWagon(), getCouleur())-Collections.frequency(cartePaye,getCouleur());
        int nbLoco=Collections.frequency(joueur.getCartesWagon(), CouleurWagon.LOCOMOTIVE)-Collections.frequency(cartePaye, CouleurWagon.LOCOMOTIVE);
        if ( nbCouleur>=carteAjouer || nbCouleur + nbLoco >= carteAjouer || (fullLoco && nbLoco>=Collections.frequency(carteBonus, CouleurWagon.LOCOMOTIVE))) {
            return true;
        } else {
            return false;
        }
    }

    private int nbCarteSupp(ArrayList<CouleurWagon> carteBonus){
        if(fullLoco){
            return Collections.frequency(carteBonus, CouleurWagon.LOCOMOTIVE);
        }
        else{
            return Collections.frequency(carteBonus, getCouleur()) + Collections.frequency(carteBonus, CouleurWagon.LOCOMOTIVE);
        }
    }

    private void coutSupplementaire(Joueur joueur, CouleurWagon c){
        if(c!=CouleurWagon.LOCOMOTIVE){
            fullLoco=false;
        }
        joueur.getCartesWagon().remove(c);
        joueur.getJeu().defausserCarteWagon(c);
        joueur.getCartesWagonPosees().add(c);
    }

    @Override
    public boolean capturer(Joueur joueur, CouleurWagon c) {
        boolean gris=false;
        if(c!=CouleurWagon.LOCOMOTIVE){
            fullLoco=false;
        }
        if(getCouleur()==CouleurWagon.GRIS && c!=CouleurWagon.LOCOMOTIVE){
            gris=true;
            setCouleur(c);
        }
        if (cartePaye.size() != getLongueur()) {
            cartePaye.add(c);
        }
        if (cartePaye.size() == getLongueur()) {
            ArrayList<CouleurWagon> carteBonus = cartesPiochees(joueur);
            if(valideApresPioche(joueur,carteBonus) && carteBonus.contains(getCouleur())){
                ArrayList<String> choix = new ArrayList<>();
                if(Collections.frequency(joueur.getCartesWagon(),CouleurWagon.LOCOMOTIVE)-Collections.frequency(cartePaye,CouleurWagon.LOCOMOTIVE)>0){
                    choix.add(CouleurWagon.LOCOMOTIVE.name());
                }
                if(Collections.frequency(joueur.getCartesWagon(),getCouleur())-Collections.frequency(cartePaye,getCouleur())>0){
                    choix.add(getCouleur().name());
                }
                String couleurChoix = joueur.choisir("Vous avez pioché " + carteBonus + " voulez vous continuer la capture ? si non, passez.", choix, choix, true);
                if (!couleurChoix.equals("")) {
                    for (int i = 0; i < cartePaye.size(); i++) {
                        super.capturer(joueur, cartePaye.get(i));
                    }
                    coutSupplementaire(joueur,CouleurWagon.valueOf(couleurChoix));
                    int nbSupp=nbCarteSupp(carteBonus);
                    int n=1;
                    while(n!=nbSupp){
                        coutSupplementaire(joueur,CouleurWagon.valueOf(couleurChoix));
                        choix.clear();
                        if(Collections.frequency(joueur.getCartesWagon(),CouleurWagon.LOCOMOTIVE)-Collections.frequency(cartePaye,CouleurWagon.LOCOMOTIVE)>0){
                            choix.add(CouleurWagon.LOCOMOTIVE.name());
                        }
                        if(Collections.frequency(joueur.getCartesWagon(),getCouleur())-Collections.frequency(cartePaye,getCouleur())>0){
                            choix.add(getCouleur().name());
                        }
                        couleurChoix=joueur.choisir("Choisissez une Locomotive ou une carte "+getCouleur(),choix,choix,false);
                        nbSupp=nbCarteSupp(carteBonus);
                        n++;
                    }
                    setProprietaire(joueur);
                    return true;
                }
                else {
                    if(gris){
                        setCouleur(CouleurWagon.GRIS);
                    }
                    cartePaye.clear();
                    joueur.addScore(-comptePoints());
                    return false;
                }
            }
            else{
                if(gris){
                    setCouleur(CouleurWagon.GRIS);
                }
                cartePaye.clear();
                joueur.addScore(-comptePoints());
                return false;
            }
        }
        if(gris){
            setCouleur(CouleurWagon.GRIS);
        }
        return true;
    }
}
