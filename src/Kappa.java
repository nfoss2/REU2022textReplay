
public class Kappa {

	public static double calculateKappa(int expertGaming, int expertNotGaming, int modelTP, int modelFP) {
		int modelGExpertG = modelTP;
		int modelGExpertN = modelFP;
		int modelNExpertG = expertGaming - modelTP;
		int modelNExpertN = expertNotGaming - modelFP;
		
		int totalModelG = modelGExpertG + modelGExpertN;
		int totalModelN = modelNExpertG + modelNExpertN;
		
		int totalClip = expertGaming + expertNotGaming;
		
		float totalAgreement = ((float)(modelGExpertG + modelNExpertN)) / ((float) totalClip);
		float expectedAgreementN = ((float) totalModelN) / ((float) totalClip) * ((float) expertNotGaming) / ((float) totalClip);
		float expectedAgreementG = ((float) totalModelG) / ((float) totalClip) * ((float) expertGaming) / ((float) totalClip);
		
		float totalExpectedAgreement = expectedAgreementG + expectedAgreementN;
		
		return (totalAgreement - totalExpectedAgreement) / (1.0 - totalExpectedAgreement);
	}
}
