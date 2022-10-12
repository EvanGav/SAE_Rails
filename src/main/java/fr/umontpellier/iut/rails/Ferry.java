package fr.umontpellier.iut.rails;

import java.util.ArrayList;
import java.util.Collections;

public class Ferry extends Route {
    /**
     * Nombre de locomotives qu'un joueur doit payer pour capturer le ferry
     */
    private int nbLocomotives;
    private int longueurAvant;

    public Ferry(Ville ville1, Ville ville2, int longueur, CouleurWagon couleur, int nbLocomotives) {
        super(ville1, ville2, longueur, couleur);
        this.nbLocomotives = nbLocomotives;
        longueurAvant=getLongueur();
    }

    @Override
    public String toString() {
        return String.format("[%s - %s (%d, %s, %d)]", getVille1(), getVille2(), getLongueur(), getCouleur(),
                nbLocomotives);
    }

    @Override
    public ArrayList<String> possedeCouleur(Joueur joueur){
        ArrayList<String> couleurs=new ArrayList<>();
        if(longueurAvant==getLongueur()){
            for(CouleurWagon c : CouleurWagon.getCouleursSimples()){
                if(((Collections.frequency(joueur.getCartesWagon(),c)>=getLongueur()-nbLocomotives && Collections.frequency(joueur.getCartesWagon(),CouleurWagon.LOCOMOTIVE)==nbLocomotives)||Collections.frequency(joueur.getCartesWagon(),CouleurWagon.LOCOMOTIVE)-nbLocomotives+Collections.frequency(joueur.getCartesWagon(),c)>=getLongueur()-nbLocomotives)&& joueur.getCartesWagon().contains(c)){
                    couleurs.add(c.name());
                }
            }
            return couleurs;
        }
        else{
            for(CouleurWagon c : CouleurWagon.getCouleursSimples()){
                if((Collections.frequency(joueur.getCartesWagon(), c)>=getLongueur() || Collections.frequency(joueur.getCartesWagon(), c) + Collections.frequency(joueur.getCartesWagon(), CouleurWagon.LOCOMOTIVE)-nbLocomotives>=getLongueur()) && joueur.getCartesWagon().contains(c)){
                    couleurs.add(c.name());
                }
            }
            return couleurs;
        }
    }

    @Override
    public boolean valide(Joueur joueur){
        if(getProprietaire()==null && Collections.frequency(joueur.getCartesWagon(),CouleurWagon.LOCOMOTIVE)>=nbLocomotives){
            ArrayList<String> couleursPossede=possedeCouleur(joueur);
            if(couleursPossede.contains(getCouleur().name()) || couleursPossede.contains(CouleurWagon.LOCOMOTIVE.name()) || (!couleursPossede.isEmpty() && getCouleur()==CouleurWagon.GRIS)){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean capturer(Joueur joueur, CouleurWagon c){
        if(nbLocomotives>0){
            int nbl=0;
            int nbLoco=nbLocomotives;
            while(nbl!=nbLoco){
                super.capturer(joueur,CouleurWagon.LOCOMOTIVE);
                nbl++;
                nbLocomotives--;
            }
        }
        return super.capturer(joueur,c);
    }

    @Override
    public int comptePoints(){
        int pts=super.comptePoints();
        setLongueur(getLongueur()-nbLocomotives);
        return pts;
    }

    public int getNbLocomotives() {
        return nbLocomotives;
    }
}
