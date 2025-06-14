package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.exception.NotFoundException;


import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PublicCompilationServiceImpl implements PublicCompilationService {
    private final CompilationRepository compilationRepository;
    private final CompilationMapper compilationMapper;

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getCompilationById(long id) {
        CompilationDto compilationDto = compilationMapper
                .toCompilationDto(compilationRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException("Подборка с " + id + "не найдена")));
        return compilationDto;
    }

    @Override
    public List<CompilationDto> getAllCompilations(Boolean pinned, int from, int size) {
        PageRequest page = PageRequest.of(from, size);
        Page<Compilation> pageCompilations;
        if (pinned != null) {
            pageCompilations = compilationRepository.findAllByPinned(pinned, page);
        } else {
            pageCompilations = compilationRepository.findAll(page);
        }

        List<CompilationDto> compilationsDto = compilationMapper.toCompilationDto(pageCompilations);
        log.info("получен список compilationsDto from = " + from + " size " + size);

        return compilationsDto;
    }
}
