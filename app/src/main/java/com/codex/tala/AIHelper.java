package com.codex.tala;
import java.time.LocalDateTime;

import android.content.Context;
import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.theokanning.openai.OpenAiResponse;
import com.theokanning.openai.assistants.message.MessageListSearchParameters;
import com.theokanning.openai.assistants.message.MessageRequest;

import com.theokanning.openai.assistants.run.RequiredAction;
import com.theokanning.openai.assistants.run.Run;
import com.theokanning.openai.assistants.run.RunCreateRequest;
import com.theokanning.openai.assistants.run.SubmitToolOutputRequestItem;
import com.theokanning.openai.assistants.run.SubmitToolOutputsRequest;
import com.theokanning.openai.assistants.run.ToolCall;
import com.theokanning.openai.assistants.run.ToolCallFunction;
import com.theokanning.openai.assistants.thread.Thread;
import com.theokanning.openai.assistants.thread.ThreadRequest;
import com.theokanning.openai.function.FunctionDefinition;
import com.theokanning.openai.function.FunctionExecutorManager;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.assistants.message.Message;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AIHelper {
    private final OpenAiService service = new OpenAiService(BuildConfig.OPENAI_API_KEY);
    private final String assistantId = "asst_2ahlqVcUrk7KK6G8PlT3vWOi";
    private final DBHelper db;
    public static Thread thread;

    public AIHelper(Context context){
        this.db = new DBHelper(context);
    }
    private Single<String> simpleChat(String prompt, int userId) {
        return Single.fromCallable(() -> {
            if (thread == null){
                ThreadRequest threadRequest = ThreadRequest.builder().build();
                thread = service.createThread(threadRequest);
            }
            String threadId = thread.getId();

            MessageRequest messageRequest = MessageRequest.builder()
                    .content(prompt)
                    .build();

            //add message to thread
            service.createMessage(threadId, messageRequest);
            RunCreateRequest runCreateRequest = RunCreateRequest.builder().assistantId(assistantId).build();

            Run run = service.createRun(threadId, runCreateRequest);

            //bro
            Run retrievedRun = service.retrieveRun(threadId, run.getId());

            while(true){
                try{
                    if (retrievedRun.getStatus().equals("completed")){
                        OpenAiResponse<Message> response = service.listMessages(threadId, MessageListSearchParameters.builder()
                                .runId(retrievedRun.getId()).build());
                        List<Message> messages = response.getData();
                        String assistantReply = "Something went wrong... please try again later.";
                        for (Message message : messages) {
                            if (message.getContent().get(0).getText().getValue() != null) {
                                assistantReply = message.getContent().get(0).getText().getValue();
                                break;
                            }
                        }

                        return assistantReply;
                    }else if(retrievedRun.getStatus().equals("requires_action")){
                        RequiredAction requiredAction = retrievedRun.getRequiredAction();
                        if (requiredAction != null) {
                            List<ToolCall> toolCalls = requiredAction.getSubmitToolOutputs().getToolCalls();
                            FunctionExecutorManager executor;
                            if (toolCalls != null && !toolCalls.isEmpty()) {
                                List<SubmitToolOutputRequestItem> toolOutputItems = new ArrayList<>();
                                for (ToolCall toolCall : toolCalls) {
                                    ToolCallFunction function = toolCall.getFunction();
                                    String toolCallId = toolCall.getId();

                                    JsonNode jT = function.getArguments().get("title");
                                    JsonNode jsD = function.getArguments().get("startDate");
                                    JsonNode jeD = function.getArguments().get("endDate");
                                    JsonNode jsT = function.getArguments().get("startTime");
                                    JsonNode jeT = function.getArguments().get("endTime");
                                    JsonNode jD = function.getArguments().get("description");
                                    JsonNode eId = function.getArguments().get("eventId");

                                    String title = jT != null? jT.asText() : "";
                                    String sD = jsD != null? jsD.asText() : "";
                                    String eD = jeD != null? jeD.asText() : "";
                                    String sT = jsT != null? jsT.asText() : "";
                                    String eT = jeT != null? jeT.asText() : "";
                                    String desc = jD != null? jD.asText() : "";
                                    int eventId = eId != null? eId.asInt() : 0;

                                    executor = new FunctionExecutorManager(ToolUtil(eventId, userId, title, sD, eD, sT, eT, desc));
                                    JsonNode result = executor.executeAndConvertToJson(function.getName(), function.getArguments());
                                    // Accumulate tool output items
                                    toolOutputItems.add(SubmitToolOutputRequestItem.builder()
                                            .toolCallId(toolCallId)
                                            .output(result.toPrettyString())
                                            .build());
                                }

                                // Submit all tool outputs at once
                                SubmitToolOutputsRequest submitToolOutputsRequest = SubmitToolOutputsRequest.builder()
                                        .toolOutputs(toolOutputItems)
                                        .build();
                                retrievedRun = service.submitToolOutputs(threadId, retrievedRun.getId(), submitToolOutputsRequest);
                            }
                        }
                    }else{
                        retrievedRun = service.retrieveRun(threadId, run.getId());
                        java.lang.Thread.sleep(500);
                    }
                }catch(Exception e){
                    throw e;
                }
            }
        });
    }

    public Single<String> executeSimpleChat(String prompt, int userId) {
        return simpleChat(prompt, userId)
                .subscribeOn(Schedulers.io()) // Run the network call on the IO thread
                .observeOn(AndroidSchedulers.mainThread()); // Handle the result on the main thread
    }

    private List<FunctionDefinition> ToolUtil(int eventId, int userId, String title, String startDate, String endDate, String startTime, String endTime, String description){
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy HH:mm:ss");
        String formattedNow = now.format(formatter);
        Log.d("params", userId + " | " + eventId + " | " + title + " | " + startDate + " | " + endDate + " | " + startTime + " | " + endTime + " | " + description);

        FunctionDefinition get_datetime_today = FunctionDefinition.builder()
                .name("get_today")
                .description("get the day, date, and time today")
                .executor(c -> formattedNow)
                .build();

        FunctionDefinition add_event = FunctionDefinition.builder()
                .name("add_event")
                .description("Add an event")
                .executor(c -> db.insertEventData(userId, title, startDate, endDate, startTime, endTime,null, null, "Basil", description))
                .build();

        FunctionDefinition search_event = FunctionDefinition.builder()
                .name("search_event")
                .description("search an event")
                .executor(c -> db.searchEvent(userId, title, startDate, endDate, startTime, endTime))
                .build();

        FunctionDefinition edit_event = FunctionDefinition.builder()
                .name("edit_event")
                .description("edit an event")
                .executor(c -> db.editEventData(userId, eventId, title, startDate, endDate, startTime, endTime, description))
                .build();

        FunctionDefinition delete_event = FunctionDefinition.builder()
                .name("delete_event")
                .description("delete an event")
                .executor(c -> db.deleteEventData(userId, eventId))
                .build();

        return Arrays.asList(get_datetime_today, add_event, search_event, edit_event, delete_event);
    }

    public Completable deleteThread() {
        return Completable.fromCallable(() -> {
            if (thread != null)
                service.deleteThread(thread.getId());
            return null;
        }).subscribeOn(Schedulers.io());
    }
}
