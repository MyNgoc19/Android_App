package com.example.user.model;

import java.util.List;





public class Room {

    private int id;
    private String title;
    private String description;
    private String address;
    private String ward;
    private String district;
    private String city;
    private double monthlyPrice;
    private Double areaM2;
    private int capacity;
    private List<String> amenities;
    private String status;
    private List<String> images;
    private String imageUrl;
    private String thumbnailUrl;
    private String createdAt;

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getAddress() { return address; }
    public String getWard() { return ward; }
    public String getDistrict() { return district; }
    public String getCity() { return city; }
    public double getMonthlyPrice() { return monthlyPrice; }
    public Double getAreaM2() { return areaM2; }
    public int getCapacity() { return capacity; }
    public List<String> getAmenities() { return amenities; }
    public String getStatus() { return status; }
    public List<String> getImages() { return images; }
    public String getImageUrl() { return imageUrl; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public String getCreatedAt() { return createdAt; }




    public String getFirstImage() {
        if (images != null && !images.isEmpty()) return images.get(0);
        if (thumbnailUrl != null) return thumbnailUrl;
        return imageUrl;
    }


    public String getFullAddress() {
        StringBuilder sb = new StringBuilder(address == null ? "" : address);
        for (String part : new String[]{ward, district, city}) {
            if (part != null && !part.trim().isEmpty()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(part.trim());
            }
        }
        return sb.toString();
    }


    public boolean isApartment() {
        String text = ((title == null ? "" : title) + " " + (description == null ? "" : description)).toLowerCase();
        return text.contains("căn hộ") || text.contains("can ho") || text.contains("apartment");
    }


    private transient boolean favorite;

    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean value) { this.favorite = value; }
}
