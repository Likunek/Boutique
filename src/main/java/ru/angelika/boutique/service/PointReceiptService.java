package ru.angelika.boutique.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.angelika.boutique.dto.PointReceiptDto;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.mapper.PointReceiptMapper;
import ru.angelika.boutique.model.PointReceipt;
import ru.angelika.boutique.repository.PointReceiptRepository;

@Service
public class PointReceiptService {
    private final PointReceiptRepository pointReceiptRepository;

    @Autowired
    public PointReceiptService(PointReceiptRepository pointReceiptRepository) {
        this.pointReceiptRepository = pointReceiptRepository;
    }

    public void addPointReceipt(PointReceiptDto pointReceiptDto) {
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
        pointReceipt.setDescription(pointReceiptDto.getDescription());
        pointReceiptRepository.save(pointReceipt);
    }

    public void deletePointReceipt(Long id) {
        pointReceiptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(PointReceipt.class, id));
        pointReceiptRepository.deleteById(id);
    }
}
