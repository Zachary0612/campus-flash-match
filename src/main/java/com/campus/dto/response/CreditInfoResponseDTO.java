package com.campus.dto.response;

import lombok.Data;
import com.campus.vo.CreditRecordVO;
import java.util.List;

@Data
public class CreditInfoResponseDTO {
    private Long userId;
    private Integer currentCredit;
    private List<CreditRecordVO> recentRecords;
}