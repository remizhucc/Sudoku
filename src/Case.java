import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class Case {
    @JsonProperty("indiceLigne")
    int indiceLigne;

    @JsonProperty("indiceColonne")
    int indiceColonne;

    @JsonProperty("valuePossi")
    ArrayList<Integer> valuePossi;

    @JsonProperty("caseValue")
    int caseValue;

    public Case() {
        indiceLigne = -1;
        indiceColonne = -1;
        valuePossi = new ArrayList<>();
        for (int i = 1; i <= 9; i++) {
            valuePossi.add(i);
        }
        caseValue = 0;
    }

// public Case(Case c) {
//  indiceLigne = c.indiceLigne;
//  indiceColonne = c.indiceColonne;
//  caseValue = c.caseValue;
//  valuePossi.addAll(c.valuePossi);
// }

    public Case(int indLigne, int indColonne, int value) {
        indiceLigne = indLigne;
        indiceColonne = indColonne;
        caseValue = value;
        valuePossi = new ArrayList<Integer>();
        if(caseValue==0){
            for (int i = 1; i <= 9; i++) {
                valuePossi.add(i);
            }
        }
    }

    public String toJson() {
        String json = "";
        json = "{"
                + " \"indiceLigne\":" + indiceLigne + ","
                + " \"indiceColonne\":" + indiceColonne + ","
                + " \"valuePossi\":" + valuePossi.toString() + ","
                + " \"caseValue\":" + caseValue
                + "}";
        return json;
    }

    public static Case toObject(String json) {
        System.out.println("Case.toObject()-->" + json);
        ObjectMapper mapper = new ObjectMapper();
        Case res = new Case();
        try {
            res = mapper.readValue(json, Case.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    public ArrayList<Integer> getValuePossi() {
        return valuePossi;
    }

    public void setCaseValue(Integer value) {
        caseValue = value;
    }

    public ArrayList<Integer> addValuePossi(Integer value) {
        valuePossi.add(value);
        return valuePossi;
    }

    public ArrayList<Integer> removeValuePossi(Integer value) {
        valuePossi.remove(value);
        return valuePossi;
    }

    public void emptyValuePossi() {
        valuePossi.clear();
    }

    public Integer getCaseValue() {
        return caseValue;
    }


    public static String arrayListToJSON(ArrayList<Case> cases) {
        String json = "";
        json += "[";
        for (int i = 0; i < cases.size(); i++) {
            json += cases.get(i).toJson();
            if (i != 8) {
                json += ",";
            }
        }
        json += "]";
        return json;
    }

    public static ArrayList<Case> JSONToArrayList(String json) {
        ArrayList<Case> caseList = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Case[] cases = objectMapper.readValue(json, Case[].class);
            caseList = new ArrayList<Case>(Arrays.asList(cases));
            return caseList;
        } catch (IOException e) {
            System.out.println(e);
        }
        return null;

    }

}