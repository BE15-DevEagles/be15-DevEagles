package com.deveagles.be15_deveagles_be.features.chat.command.application.service;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class PromptTemplate {

  private static final Random random = new Random();

  public static String getAiResponsePrompt(String userMessage) {
    return String.format(
        "너는 우리 회사의 마스코트인 수리라는 이름의 AI 어시스턴트야. 다음 사용자 메시지에 자연스럽고 도움이 되는 응답을 생성해줘. "
            + "응답은 한국어로 50자에서 100자 사이로 간결하게 해줘. 대화 상대방의 메시지: %s",
        userMessage);
  }

  public static String getAiResponsePromptWithHistory(String chatHistory, String userMessage) {
    return String.format(
        "너는 우리 회사의 마스코트인 수리라는 이름의 AI 어시스턴트야. "
            + "이전 대화 맥락을 참고해서 사용자의 메시지에 자연스럽고 도움이 되는 응답을 생성해줘. "
            + "응답은 한국어로 50자에서 100자 사이로 간결하게 해줘. "
            + "이전 대화 맥락: %s "
            + "현재 사용자 메시지: %s",
        chatHistory, userMessage);
  }

  public static final String MOOD_INQUIRY_PROMPT =
      "사용자의 기분과 감정 상태를 물어보는 창의적인 질문을 1개만 생성해주세요. " + "질문은 짧고 친근하게, 다양한 감정 표현이 가능하도록 해주세요.";

  public static String getMoodQuestionPrompt() {
    return MOOD_INQUIRY_PROMPT;
  }

  public static String getDefaultMoodQuestion() {
    return getRandomMoodInquiry();
  }

  private static final List<String> DEFAULT_AI_RESPONSES =
      Arrays.asList(
          "안녕하세요! 무엇을 도와드릴까요?",
          "더 자세한 내용을 알려주시겠어요?",
          "흥미로운 질문이네요. 좀 더 생각해볼게요.",
          "제가 도움이 필요하신가요?",
          "좋은 질문입니다. 답변을 드리겠습니다.",
          "그 부분에 대해 더 알고 싶으시다면, 자세히 물어봐 주세요.");

  private static final List<String> DEFAULT_MOOD_INQUIRIES =
      Arrays.asList(
          "오늘 기분은 어떠신가요?",
          "오늘 아침 컨디션은 어떠세요?",
          "오늘 하루는 어떻게 시작하셨나요?",
          "지금 기분을 이모티콘으로 표현한다면 어떤 것일까요?",
          "오늘 에너지는 1-10 사이 중 몇인가요?");

  public static String getRandomDefaultResponse() {
    return DEFAULT_AI_RESPONSES.get(random.nextInt(DEFAULT_AI_RESPONSES.size()));
  }

  public static String getRandomMoodInquiry() {
    return DEFAULT_MOOD_INQUIRIES.get(random.nextInt(DEFAULT_MOOD_INQUIRIES.size()));
  }

  public static String getMoodFeedback(String moodType) {
    switch (moodType) {
      case "JOY":
        return getRandomJoyFeedback();
      case "SADNESS":
        return getRandomSadnessFeedback();
      case "ANGER":
        return getRandomAngerFeedback();
      case "FEAR":
        return getRandomFearFeedback();
      case "SURPRISE":
        return getRandomSurpriseFeedback();
      case "DISGUST":
        return getRandomDisgustFeedback();
      case "NEUTRAL":
      default:
        return getRandomNeutralFeedback();
    }
  }

  private static String getRandomJoyFeedback() {
    List<String> feedbacks =
        Arrays.asList(
            "기분이 좋으시다니 정말 다행이네요! 오늘도 좋은 하루 되세요 😊",
            "긍정적인 에너지로 가득 차 있군요! 그 기분 그대로 유지하세요!",
            "행복한 기분이 오래 지속되길 바랍니다. 무엇이 당신을 행복하게 만들었나요?");
    return feedbacks.get(random.nextInt(feedbacks.size()));
  }

  private static String getRandomSadnessFeedback() {
    List<String> feedbacks =
        Arrays.asList(
            "기분이 우울하시다니 안타깝네요. 제가 도움이 필요하신 것이 있으신가요?",
            "슬픈 일이 있으셨나요? 때로는 감정을 표현하는 것도 도움이 됩니다.",
            "우울한 기분이 빨리 나아지길 바랍니다. 제가 무언가 도울 수 있다면 언제든 말씀해주세요.");
    return feedbacks.get(random.nextInt(feedbacks.size()));
  }

  private static String getRandomAngerFeedback() {
    List<String> feedbacks =
        Arrays.asList(
            "화가 나셨군요. 힘든 일이 있으신가요? 저에게 말씀해주시면 도움이 될 수 있을지도 모르겠네요.",
            "화나는 일이 있으셨군요. 심호흡을 몇 번 해보는 건 어떨까요?",
            "화가 날 때는 잠시 멈추고 생각하는 시간을 가져보는 것도 좋습니다.");
    return feedbacks.get(random.nextInt(feedbacks.size()));
  }

  private static String getRandomFearFeedback() {
    List<String> feedbacks =
        Arrays.asList(
            "불안하고 두려운 마음이 드시는군요. 함께 이야기 나누면 도움이 될 수 있어요.",
            "걱정되는 일이 있으신가요? 천천히 깊게 호흡하고 하나씩 해결해 나가는 것이 좋을 것 같아요.",
            "두려움을 느끼실 때는 지금 할 수 있는 일에 집중해보세요.");
    return feedbacks.get(random.nextInt(feedbacks.size()));
  }

  private static String getRandomSurpriseFeedback() {
    List<String> feedbacks =
        Arrays.asList(
            "놀라운 일이 있으셨나요? 어떤 일인지 더 들려주실래요?",
            "예상치 못한 상황에 놀라셨군요! 좋은 놀라움이었길 바랍니다.",
            "놀라운 일을 경험하셨네요! 이런 순간이 삶을 더 흥미롭게 만들죠.");
    return feedbacks.get(random.nextInt(feedbacks.size()));
  }

  private static String getRandomDisgustFeedback() {
    List<String> feedbacks =
        Arrays.asList(
            "불쾌한 일이 있으셨군요. 그런 감정도 자연스러운 것입니다.",
            "마음에 들지 않는 상황을 겪으셨나요? 어떻게 도와드릴까요?",
            "불편한 상황이 있으셨던 것 같네요. 기분 전환을 위해 잠시 휴식을 취하는 것은 어떨까요?");
    return feedbacks.get(random.nextInt(feedbacks.size()));
  }

  private static String getRandomNeutralFeedback() {
    List<String> feedbacks =
        Arrays.asList(
            "답변 감사합니다! 오늘 하루도 좋은 일들이 가득하길 바랍니다.",
            "평온한 하루를 보내고 계시는군요. 때로는 그런 날도 필요합니다.",
            "오늘 하루는 어떻게 지내고 계신가요? 필요한 게 있으시면 언제든 말씀해주세요.");
    return feedbacks.get(random.nextInt(feedbacks.size()));
  }
}
