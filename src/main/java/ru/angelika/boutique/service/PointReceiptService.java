package ru.angelika.boutique.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.angelika.boutique.dto.PointReceiptDto;
import ru.angelika.boutique.exception.ResourceExistsException;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.mapper.PointReceiptMapper;
import ru.angelika.boutique.model.PointReceipt;
import ru.angelika.boutique.model.Storage;
import ru.angelika.boutique.repository.PointReceiptRepository;

import java.util.List;

@Slf4j
@Service
public class PointReceiptService {
    private final PointReceiptRepository pointReceiptRepository;

    @Autowired
    public PointReceiptService(PointReceiptRepository pointReceiptRepository) {
        this.pointReceiptRepository = pointReceiptRepository;
    }

    public void addPointReceipt(PointReceiptDto pointReceiptDto) {
        checkDuplicate(pointReceiptDto.getAddress(), pointReceiptDto.getCity());
        pointReceiptRepository.save(PointReceiptMapper.toPointReceipt(pointReceiptDto));
    }

    public PointReceipt getPointReceipt(Long id) {
        return pointReceiptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(PointReceipt.class, id));
    }

    public void updatePointReceipt(Long id, PointReceiptDto pointReceiptDto) {
        PointReceipt pointReceipt = pointReceiptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(PointReceipt.class, id));
        pointReceipt.setAddress(pointReceiptDto.getAddress());
        pointReceiptRepository.save(pointReceipt);
    }

    public void deletePointReceipt(Long id) {
        pointReceiptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(PointReceipt.class, id));
        pointReceiptRepository.deleteById(id);
    }

    private void checkDuplicate(String address, String city) {
        List<PointReceipt> pointsByAddress = pointReceiptRepository.findByAddress(address);
        List<PointReceipt> pointsByCity = pointReceiptRepository.findByCity(city);
        if (pointsByAddress.size() > 0 && pointsByCity.size() > 0) {
            log.error("PointReceipt with address={}, city={} already exists", address, city);
            throw new ResourceExistsException(PointReceipt.class,  city + " : " + address);
        }
    }
}
