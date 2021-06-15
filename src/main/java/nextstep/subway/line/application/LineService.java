package nextstep.subway.line.application;

import nextstep.subway.common.exception.LineNotFoundException;
import nextstep.subway.line.domain.Line;
import nextstep.subway.line.domain.LineRepository;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.section.domain.Section;
import nextstep.subway.section.dto.SectionRequest;
import nextstep.subway.section.dto.SectionResponse;
import nextstep.subway.station.application.StationService;
import nextstep.subway.station.domain.Station;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LineService {

    private LineRepository lineRepository;
    private StationService stationService;

    public LineService(LineRepository lineRepository, StationService stationService) {
        this.lineRepository = lineRepository;
        this.stationService = stationService;
    }

    public LineResponse saveLine(LineRequest request) {
        if (request.isContainsStation()) {
            Station upStation = stationService.findDomainById(request.getUpStationId());
            Station downStation = stationService.findDomainById(request.getDownStationId());
            Line persistLine = lineRepository.save(request.toLineWithStation(upStation, downStation));
            return LineResponse.of(persistLine);
        }
        Line persistLine = lineRepository.save(request.toLine());
        return LineResponse.of(persistLine);
    }

    public List<LineResponse> findAllLines() {
        List<Line> lines = lineRepository.findAll();

        return lines.stream()
                .map(line -> LineResponse.of(line))
                .collect(Collectors.toList());
    }

    public LineResponse findById(Long id) {
        Line line = findResponseDomainById(id);

        return LineResponse.of(line);
    }

    public LineResponse updateLine(Long lineId, LineRequest lineRequest) {
        Line line = findLine(lineId);
        line.update(lineRequest.toLine());
        return LineResponse.of(line);
    }

    public void deleteLine(Long lineId) {
        findLine(lineId);
        lineRepository.deleteById(lineId);
    }

    private Line findResponseDomainById(Long id) {
        return findLine(id);
    }


    public SectionResponse addSection(Long lineId, SectionRequest sectionRequest) {
        Line line = findLine(lineId);
        Station upStation = stationService.findDomainById(sectionRequest.getUpStationId());
        Station downStation = stationService.findDomainById(sectionRequest.getDownStationId());
        Section section = line.addSection(upStation, downStation, sectionRequest.getDistance());
        return SectionResponse.of(section);
    }

    public void removeSectionByStationId(Long lineId, Long stationId) {
        Line line = findLine(lineId);
        Station station = stationService.findDomainById(stationId);
        line.removeStationInSection(station);
    }

    private Line findLine(Long lineId) {
        return lineRepository.findById(lineId).orElseThrow(() -> new LineNotFoundException("존재하지 않는 노선입니다."));
    }
}
