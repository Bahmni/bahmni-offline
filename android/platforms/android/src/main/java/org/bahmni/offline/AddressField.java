package org.bahmni.offline;

public enum AddressField {

    ADDRESS_1("address1"), ADDRESS_2("address2"), ADDRESS_3("address3"), NEIGHBORHOOD_CELL("neighborhoodCell"),
    ADDRESS_4("address4"), TOWNSHIP_DIVISION("townshipDivision"), ADDRESS_5("address5"), SUBREGION("subregion"),
    ADDRESS_6("address6"), REGION("region"), CITY_VILLAGE("cityVillage"), COUNTY_DISTRICT("countyDistrict"),
    STATE_PROVINCE("stateProvince"), COUNTRY("country"), POSTAL_CODE("postalCode"), LONGITUDE("longitude"),
    LATITUDE("latitude");

    String name;

    AddressField(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static final AddressField getByName(String name) {

        for (AddressField field : AddressField.values()) {
            if (equals(name, field.getName())) {
                return field;
            }
        }

        return null;
    }

    public static boolean equals(String str1, String str2) {
        return str1 == null?str2 == null:str1.equals(str2);
    }
}
