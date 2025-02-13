package com.ritik.assignment.request;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IdentifyRequest {

    private String email;
    private String phoneNumber;

}
