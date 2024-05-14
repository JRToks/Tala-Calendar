from openai import OpenAI
from secrets import API_KEY
import time
import json

client = OpenAI(api_key = API_KEY)
model = "gpt-3.5-turbo-0125"
assistant_id = "asst_2ahlqVcUrk7KK6G8PlT3vWOi"
thread = None

def get_response(prompt):
    global thread
    if thread is None:
        thread = client.beta.threads.create()

    prompt = str(prompt)
    message = client.beta.threads.messages.create(
        thread_id = thread.id,
        role = "user",
        content = prompt
    )

    run = client.beta.threads.runs.create(
        thread_id=thread.id,
        assistant_id=assistant_id
    )

    function_dispatch_table = {
        "add_event": add_event,
        "edit_event": edit_event,
        "delete_event": delete_event,
        "search_event": search_event
    }

    while True:
        # wait for 5 seconds
        time.sleep(5)

        # retrieve the run status
        run_status = client.beta.threads.runs.retrieve(
            thread_id = thread.id,
            run_id = run.id
        )

        # if run is completed, get messages
        if run_status.status == 'completed':
            messages = client.beta.threads.messages.list(
                thread_id = thread.id
            )

            # loop through messages and print content based on role
            for msg in messages.data:
                role = msg.role
                content = msg.content[0].text.value
                if role == 'assistant':
                    return content
        elif run_status.status == 'requires_action':
            required_actions = run_status.required_action.submit_tool_outputs.model_dump()
            tools_output = []

            for action in required_actions["tool_calls"]:
                func_name = action["function"]["name"]
                arguments = json.loads(action["function"]["arguments"])

                func = function_dispatch_table.get(func_name)
                if func:
                    result = func(**arguments)
                    # ensure the output is a JSON string
                    output = json.dumps(result) if not isinstance(result, str) else result
                    tools_output.append({
                        "tool_call_id": action["id"],
                        "output": output
                    })
                else:
                    return f"Function {func_name} not found "

            # submit the tool outputs to assistant API
            client.beta.threads.runs.submit_tool_outputs(
                thread_id = thread.id,
                run_id = run.id,
                tool_outputs = tools_output
            )

        else:
            time.sleep(5)

def add_event(startTime, endTime, startDate, endDate, title, description = ""):
    return True

def edit_event(startTime, endTime, startDate, endDate, title, description = ""):
    return True

def delete_event(startTime, endTime, startDate, endDate, title):
    return True

def search_event(startTime = "", endTime = "", startDate = "", endDate = "", title = ""):
    return True

def delete_thread():
    global thread
    if thread is not None:
        client.beta.threads.delete(thread.id)
        thread = None