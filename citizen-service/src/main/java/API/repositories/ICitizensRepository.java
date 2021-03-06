/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package API.repositories;

import API.entities.Citizen;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author thinkbuntu
 */
public interface ICitizensRepository {

    List<Citizen> batchUpdate(List<Citizen> citizenList, UUID authorId);

    Citizen createCitizen(Citizen citizen, UUID authorId, UUID careCenterId);

    boolean deleteCitizen(UUID id, UUID authorId);

    Citizen findCitizen(UUID id);

    List<Citizen> getCitizens();
    
    List<Citizen> getMyCitizens(List<UUID> listOfCitizensIds);

    List<Citizen> getCareCenterCitizens(UUID careCenterId, List<UUID> listOfCitizensIds);
}
