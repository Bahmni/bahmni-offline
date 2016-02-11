package org.bahmni.offline;

public class AddressHierarchyEntry {

    private String uuid;
    private Integer addressHierarchyEntryId;
    private String name;
    private String userGeneratedId;
    private AddressHierarchyEntry parent;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUserGeneratedId() {
        return userGeneratedId;
    }

    public void setUserGeneratedId(String userGeneratedId) {
        this.userGeneratedId = userGeneratedId;
    }

    public AddressHierarchyEntry getParent() {
        return parent;
    }

    public void setParent(AddressHierarchyEntry parent) {
        this.parent = parent;
    }

    public Integer getAddressHierarchyEntryId() {
        return addressHierarchyEntryId;
    }

    public void setAddressHierarchyEntryId(Integer addressHierarchyEntryId) {
        this.addressHierarchyEntryId = addressHierarchyEntryId;
    }
}
