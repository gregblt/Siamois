package fr.siamois.utils.stratigraphy.simpledetection;

import java.util.HashMap;
import java.util.Map;

class StratigraphieAvecMatrice {
    private String[] couches; // Tableau des noms des couches
    private int[][] matrice;  // Matrice des relations
    private Map<String, Integer> indexMap; // Associe le nom d'une couche à son index

    public StratigraphieAvecMatrice(String[] couches) {
        this.couches = couches;
        this.matrice = new int[couches.length][couches.length];
        this.indexMap = new HashMap<>();

        // Initialiser le mapping des noms vers les indices
        for (int i = 0; i < couches.length; i++) {
            indexMap.put(couches[i], i);
        }
    }

    public boolean ajouterRelation(String couche1, String relation, String couche2) {
        if (!indexMap.containsKey(couche1) || !indexMap.containsKey(couche2)) {
            System.out.println("Erreur : Une ou les deux couches n'existent pas.");
            return false;
        }

        int i = indexMap.get(couche1);
        int j = indexMap.get(couche2);
        int valeurRelation = relationToCode(relation);

        // Contrôle de cohérence
        if (!verifierCohérence(i, j, valeurRelation)) {
            System.out.println("Erreur : Relation incohérente entre " + couche1 + " et " + couche2);
            return false;
        }

        // Ajouter la relation
        matrice[i][j] = valeurRelation;

        // Pour les relations symétriques (ex : synchrone), ajouter dans l'autre sens
        if (relation.equals("synchrone") || relation.equals("synchrone incertain")) {
            matrice[j][i] = valeurRelation;
        }

        System.out.println("Relation ajoutée : " + couche1 + " -[" + relation + "]-> " + couche2);
        return true;
    }

    private int relationToCode(String relation) {
        switch (relation) {
            case "sous":
                return 1;
            case "sous incertain":
                return 2;
            case "synchrone":
                return 3;
            case "synchrone incertain":
                return 4;
            default:
                return 0; // Relation inexistante ou inconnue
        }
    }

    private boolean verifierCohérence(int i, int j, int relation) {
        // Contrôler les relations inverses
        if (relation == 1 && matrice[j][i] == 1) {
            return false; // Une couche ne peut pas être "sous" une autre dans les deux sens
        }
        if (relation == 3 && matrice[j][i] != 0 && matrice[j][i] != 3) {
            return false; // Synchrone doit être cohérent dans les deux sens
        }
        return true;
    }

    public void afficherMatrice() {
        System.out.println("Matrice des relations :");
        System.out.print("    ");
        for (String couche : couches) {
            System.out.print(couche + "\t");
        }
        System.out.println();

        for (int i = 0; i < couches.length; i++) {
            System.out.print(couches[i] + " ");
            for (int j = 0; j < couches.length; j++) {
                System.out.print(matrice[i][j] + "\t");
            }
            System.out.println();
        }
    }
}

