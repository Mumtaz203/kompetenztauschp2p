package de.thws.kompetenz.auth.application.port.out;

public interface IPasswordHasherPort {

    String hash(String plainPassword);

    boolean verify(String plainPassword, String hashedPassword);
}
