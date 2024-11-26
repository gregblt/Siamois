package fr.siamois.utils.stratigraphy.simpledetection;

public class GestionStratigraphieMatrice {
    public static void main(String[] args) {
        // Définir les couches
        String[] nomsCouches = {"Couche A", "Couche B", "Couche C"};
        StratigraphieAvecMatrice stratigraphie = new StratigraphieAvecMatrice(nomsCouches);

        // Ajouter des relations
        stratigraphie.ajouterRelation("Couche A", "sous", "Couche B"); // A est sous B
        stratigraphie.ajouterRelation("Couche B", "sous", "Couche C"); // B est sous C
        stratigraphie.ajouterRelation("Couche C", "synchrone", "Couche A"); // Erreur : incohérent avec "sous"

        // Afficher la matrice
        stratigraphie.afficherMatrice();
    }
}
