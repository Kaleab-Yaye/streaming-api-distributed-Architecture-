/* package com.adnakiwoch.platform.streaming_api.service.internal;


import com.adnakiwoch.platform.streaming_api.domain.Vid;
import com.adnakiwoch.platform.streaming_api.repository.VidRepo;
import com.github.benmanes.caffeine.cache.Cache;
import org.eclipse.angus.mail.util.DefaultProvider;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class EncodeServiceHelper {
    private final VidRepo vidRepo;


    EncodeServiceHelper (VidRepo vidRepo,  Cache<Integer, UUID> cache ){
        this.vidRepo = vidRepo;
        this.cache = cache;
    }

    public boolean checkMachineWorking(int machineNumber){

        Optional<Vid> vidOptional = vidRepo.checkMachineWork(machineNumber);



    }
}

 */
